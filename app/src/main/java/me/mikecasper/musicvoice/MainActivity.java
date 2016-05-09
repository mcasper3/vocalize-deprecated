package me.mikecasper.musicvoice;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.login.events.LogOutEvent;
import me.mikecasper.musicvoice.login.events.RefreshTokenEvent;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.SpotifyUser;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.playlist.PlaylistFragment;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.DestroyPlayerEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.settings.SettingsFragment;
import retrofit2.Call;

public class MainActivity extends MusicVoiceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private IEventManager mEventManager;
    private LinkedList<RefreshTokenEvent> mEvents;
    private ProgressBar mProgressBar;
    private boolean mRefreshingToken;
    private boolean mLeftieLayout;
    private boolean mIsPlaying;
    private Track mTrack;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEventManager = EventManagerProvider.getInstance(this);
        mEvents = new LinkedList<>();
        mRefreshingToken = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mProgressBar = (ProgressBar) findViewById(R.id.mini_song_time);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.music_home);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            PlaylistFragment playlistFragment = new PlaylistFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_content, playlistFragment)
                    .commit();

            String imageUrl = sharedPreferences.getString(SpotifyUser.PROFILE_IMAGE, null);
            String userName = sharedPreferences.getString(SpotifyUser.NAME, null);

            View headerView = navigationView.getHeaderView(0);
            CircleImageView profileImage = (CircleImageView) headerView.findViewById(R.id.profile_image);
            if (imageUrl != null) {
                Picasso.with(this).load(imageUrl).fit().into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_action_default_profile);
            }

            if (userName != null) {
                TextView profileName = (TextView) headerView.findViewById(R.id.user_name);
                profileName.setText(userName);
            }
        }

        mLeftieLayout = sharedPreferences.getBoolean(SettingsFragment.LEFTIE_LAYOUT_SELECTED, false);

        View miniNowPlaying = findViewById(R.id.main_music_controls);

        miniNowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
                intent.putExtra(NowPlayingActivity.TRACK, mTrack);
                startActivity(intent);
            }
        });

        ImageView leftImage = (ImageView) miniNowPlaying.findViewById(R.id.left_image);
        ImageView rightImage = (ImageView) miniNowPlaying.findViewById(R.id.right_image);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventManager.postEvent(new TogglePlaybackEvent());

                mIsPlaying = !mIsPlaying;

                updatePlayButton((ImageView) v);
            }
        };

        if (mLeftieLayout) {
            leftImage.setImageResource(R.drawable.ic_play);
            rightImage.setImageResource(R.drawable.default_playlist);

            leftImage.setOnClickListener(onClickListener);
        } else {
            leftImage.setImageResource(R.drawable.default_playlist);
            rightImage.setImageResource(R.drawable.ic_play);

            rightImage.setOnClickListener(onClickListener);
        }
    }

    @Override
    protected void onPause() {
        mEventManager.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventManager.register(this);
        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    @Subscribe
    public void onPlayerStatusObtained(UpdatePlayerStatusEvent event) {
        View miniNowPlaying = findViewById(R.id.main_music_controls);

        mIsPlaying = event.isPlaying();
        mTrack = event.getTrack();

        if (miniNowPlaying != null) {
            if (mIsPlaying) {
                Track track = event.getTrack();

                if (track != null) {
                    updateMiniNowPlaying(track, miniNowPlaying);
                }

                // display the view if it is hidden
                if (miniNowPlaying.getVisibility() == View.GONE) {
                    Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

                    miniNowPlaying.setVisibility(View.VISIBLE);
                    miniNowPlaying.startAnimation(slideUp);
                }
            } else {
                // hide the view if it is shown
                if (miniNowPlaying.getVisibility() == View.VISIBLE) {
                    miniNowPlaying.setVisibility(View.GONE);
                }
            }
        }
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        View miniNowPlaying = findViewById(R.id.main_music_controls);

        mIsPlaying = event.isPlayingSong();

        if (miniNowPlaying != null) {
            updateMiniNowPlaying(event.getTrack(), miniNowPlaying);
        }
    }

    @Subscribe
    public void onSongTimeUpdated(UpdateSongTimeEvent event) {
        mProgressBar.setProgress(event.getSongTime());
    }

    private void updateMiniNowPlaying(Track track, View miniNowPlaying) {
        mProgressBar.setMax(track.getDuration());

        ImageView leftImage = (ImageView) miniNowPlaying.findViewById(R.id.left_image);
        ImageView rightImage = (ImageView) miniNowPlaying.findViewById(R.id.right_image);
        TextView trackName = (TextView) miniNowPlaying.findViewById(R.id.mini_track_name);
        TextView artistName = (TextView) miniNowPlaying.findViewById(R.id.mini_artist_name);

        int drawableId = mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play;

        if (mLeftieLayout) {
            leftImage.setImageResource(drawableId);
            Picasso.with(this).load(track.getAlbum().getImages().get(0).getUrl()).into(rightImage);
        } else {
            Picasso.with(this).load(track.getAlbum().getImages().get(0).getUrl()).into(leftImage);
            rightImage.setImageResource(drawableId);
        }

        trackName.setText(track.getName());

        String artistNames = "";

        for (Artist artist : track.getArtists()) {
            artistNames += artist.getName() + ", ";
        }

        artistNames = artistNames.substring(0, artistNames.length() - 2);
        artistName.setText(artistNames);
    }

    @Subscribe
    public void onUserObtained(SpotifyUser user) {
        CircleImageView profileImage = (CircleImageView) findViewById(R.id.profile_image);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(SpotifyUser.ID, user.getId())
                .putString(SpotifyUser.NAME, user.getDisplay_name());

        if (profileImage != null && user.getImages() != null && user.getImages().size() > 0) {
            String firstImageUrl = user.getImages().get(0).getUrl();

            Picasso.with(this).load(firstImageUrl).fit().into(profileImage);

            editor.putString(SpotifyUser.PROFILE_IMAGE, firstImageUrl);
        }

        TextView profileName = (TextView) findViewById(R.id.user_name);
        if (profileName != null) {
            profileName.setText(user.getDisplay_name());
        }

        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                super.onBackPressed();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_log_out) {
            mEventManager.postEvent(new LogOutEvent(this));
            mEventManager.postEvent(new DestroyPlayerEvent());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Subscribe
    public void onRefreshToken(RefreshTokenEvent event) {
        if (!mRefreshingToken) {
            mRefreshingToken = true;
            mEventManager.postEvent(new LogInEvent(this));
        }
        mEvents.push(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                for (RefreshTokenEvent event : mEvents) {
                    Call call = event.getCall().clone();
                    call.enqueue(event.getCallback());
                }

                mRefreshingToken = false;
            }
        }
    }

    private void updatePlayButton(ImageView imageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePlayButton(imageView);
        } else {
            int id = mIsPlaying ? R.drawable.ic_pause : R.drawable.ic_play;

            imageView.setImageResource(id);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animatePlayButton(ImageView imageView) {
        AnimatedVectorDrawable drawable;
        if (mIsPlaying) {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause);
        } else {
            drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play);
        }

        imageView.setImageDrawable(drawable);
        drawable.start();
    }
}
