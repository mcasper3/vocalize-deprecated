package me.mikecasper.musicvoice;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import me.mikecasper.musicvoice.login.events.LogOutEvent;
import me.mikecasper.musicvoice.models.SpotifyUser;
import me.mikecasper.musicvoice.playlist.PlaylistFragment;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;

public class MainActivity extends MusicVoiceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private IEventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEventManager = EventManagerProvider.getInstance(this);

        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            PlaylistFragment playlistFragment = new PlaylistFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.mainContent, playlistFragment)
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setCheckedItem(R.id.music_home);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String imageUrl = sharedPreferences.getString(SpotifyUser.PROFILE_IMAGE, null);
            String userName = sharedPreferences.getString(SpotifyUser.NAME, null);

            View headerView = navigationView.getHeaderView(0);
            CircleImageView profileImage = (CircleImageView) headerView.findViewById(R.id.profileImage);
            if (imageUrl != null) {
                Picasso.with(this).load(imageUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_action_default_profile);
            }

            if (userName != null) {
                TextView profileName = (TextView) headerView.findViewById(R.id.userName);
                profileName.setText(userName);
            }
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
    }

    @Subscribe
    public void onUserObtained(SpotifyUser user) {
        CircleImageView profileImage = (CircleImageView) findViewById(R.id.profileImage);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(SpotifyUser.ID, user.getId())
                .putString(SpotifyUser.NAME, user.getDisplay_name());

        if (profileImage != null && user.getImages() != null && user.getImages().size() > 0) {
            String firstImageUrl = user.getImages().get(0).getUrl();

            Picasso.with(this).load(firstImageUrl).into(profileImage);

            editor.putString(SpotifyUser.PROFILE_IMAGE, firstImageUrl);
        }

        TextView profileName = (TextView) findViewById(R.id.userName);
        profileName.setText(user.getDisplay_name());

        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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
        }/* else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
