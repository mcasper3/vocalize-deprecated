package me.mikecasper.musicvoice.util;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.MusicVoiceActivity;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.login.events.LogOutEvent;
import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;
import me.mikecasper.musicvoice.playlist.PlaylistFragment;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.DestroyPlayerEvent;
import me.mikecasper.musicvoice.settings.SettingsActivity;

public class NavViewController {

    private MusicVoiceActivity mActivity;
    private DrawerLayout mDrawer;
    private IEventManager mEventManager;

    public NavViewController(MusicVoiceActivity activity, DrawerLayout drawer) {
        mActivity = activity;
        mDrawer = drawer;
        mEventManager = EventManagerProvider.getInstance(mActivity);
    }

    public void destroy() {
        mActivity = null;
        mDrawer = null;
        mEventManager = null;
    }

    public void handleAction(int id) {
        if (id == R.id.nav_log_out) {
            mEventManager.postEvent(new LogOutEvent(mActivity));
            mEventManager.postEvent(new DestroyPlayerEvent());
        } else if (id == R.id.playlists) {
            if (mActivity instanceof NowPlayingActivity) {
                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mActivity.startActivity(intent);
            } else {
                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();

                Fragment fragment = fragmentManager.findFragmentById(R.id.main_content);

                if (fragment == null || !(fragment instanceof PlaylistFragment)) {
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }

                    Fragment newFragment = new PlaylistFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_content, newFragment)
                            .commit();
                }
            }
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(mActivity, SettingsActivity.class);
            mActivity.startActivity(intent);
        }

        if (mDrawer != null) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }
}
