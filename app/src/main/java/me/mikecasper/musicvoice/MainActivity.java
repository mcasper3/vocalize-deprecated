package me.mikecasper.musicvoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.mikecasper.musicvoice.api.services.LogInService;
import me.mikecasper.musicvoice.controllers.MainMusicButtonController;
import me.mikecasper.musicvoice.login.events.GetUserEvent;
import me.mikecasper.musicvoice.login.events.LogInEvent;
import me.mikecasper.musicvoice.login.events.RefreshTokenEvent;
import me.mikecasper.musicvoice.models.SpotifyUser;
import me.mikecasper.musicvoice.playlist.PlaylistFragment;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.DisplayNotificationEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.settings.SettingFragment;
import me.mikecasper.musicvoice.util.NavViewController;
import retrofit2.Call;

public class MainActivity extends MusicVoiceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String DISPLAY_SETTINGS = "displaySettings";

    private static final String TAG = "MainActivity";

    private IEventManager mEventManager;
    private LinkedList<RefreshTokenEvent> mEvents;
    private boolean mRefreshingToken;
    private MainMusicButtonController mController;
    private NavViewController mNavViewController;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mEventManager = EventManagerProvider.getInstance(this);
        mEvents = new LinkedList<>();
        mRefreshingToken = false;

        mEventManager.postEvent(new GetUserEvent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.setCheckedItem(R.id.music_home);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        if (savedInstanceState == null && !intent.hasExtra(DISPLAY_SETTINGS)) {
            PlaylistFragment playlistFragment = new PlaylistFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, playlistFragment)
                    .commit();

        }

        String imageUrl = sharedPreferences.getString(SpotifyUser.PROFILE_IMAGE, null);
        String userName = sharedPreferences.getString(SpotifyUser.NAME, null);

        View headerView = navigationView.getHeaderView(0);
        CircleImageView profileImage = (CircleImageView) headerView.findViewById(R.id.profile_image);
        if (imageUrl != null) {
            Picasso.with(this).load(imageUrl).fit().into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_profile);
        }

        if (userName != null) {
            TextView profileName = (TextView) headerView.findViewById(R.id.user_name);
            profileName.setText(userName);
        }

        View miniNowPlaying = findViewById(R.id.main_music_controls);
        mController = new MainMusicButtonController(miniNowPlaying);

        mNavViewController = new NavViewController(this, drawer);

        handleIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(DISPLAY_SETTINGS)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_content, new SettingFragment())
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        mController.setContext(null);

        mEventManager.postEvent(new DisplayNotificationEvent());
        mEventManager.unregister(mController);
        mEventManager.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventManager.register(this);
        mEventManager.register(mController);
        mController.setContext(this);

        mEventManager.postEvent(new GetPlayerStatusEvent());
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
                if (getSupportFragmentManager().findFragmentById(R.id.main_content) instanceof SettingFragment) {
                    super.onBackPressed();
                } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mNavViewController.handleAction(id);

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

    @Override
    protected void onDestroy() {
        mEventManager = null;
        mEvents = null;
        mController.tearDown();
        mController = null;
        mNavViewController.destroy();
        mNavViewController = null;

        ((MusicVoiceApplication) getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
