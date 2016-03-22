package me.mikecasper.musicvoice.overview;

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
import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.models.SpotifyUser;
import me.mikecasper.musicvoice.services.EventManager;
import me.mikecasper.musicvoice.services.EventManagerProvider;

public class MusicOverviewActivity extends MusicVoiceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEventManager = EventManagerProvider.getInstance(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String imageUrl = preferences.getString(SpotifyUser.PROFILE_IMAGE, null);
        String userName = preferences.getString(SpotifyUser.NAME, null);

        View headerView = navigationView.getHeaderView(0);
        CircleImageView profileImage = (CircleImageView) headerView.findViewById(R.id.profileImage);
        if (imageUrl != null) {
            Picasso.with(this).load(imageUrl).into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_profile);
        }

        if (userName != null) {
            TextView profileName = (TextView) headerView.findViewById(R.id.userName);
            profileName.setText(userName);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String imageUrl = preferences.getString(SpotifyUser.PROFILE_IMAGE, null);
        if (imageUrl != null) {
            Picasso.with(this).load(imageUrl).into(profileImage);
        }

        TextView profileName = (TextView) findViewById(R.id.userName);
        profileName.setText(preferences.getString(SpotifyUser.NAME, "Jake Sanchez"));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

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
