package me.mikecasper.vocalize.util;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import me.mikecasper.vocalize.MainActivity;
import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.login.events.LogOutEvent;
import me.mikecasper.vocalize.nowplaying.NowPlayingActivity;
import me.mikecasper.vocalize.playlist.PlaylistFragment;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.DestroyPlayerEvent;
import me.mikecasper.vocalize.settings.SettingFragment;

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
            FragmentManager fragmentManager = mActivity.getSupportFragmentManager();

            Fragment fragment = fragmentManager.findFragmentById(R.id.main_content);

            boolean isNowPlayingActivity = mActivity instanceof NowPlayingActivity;

            if (isNowPlayingActivity) {
                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.putExtra(MainActivity.DISPLAY_SETTINGS, true);
                mActivity.startActivity(intent);
            } else {
                if (fragment == null || !(fragment instanceof SettingFragment)) {
                    Fragment newFragment = new SettingFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_content, newFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }

        if (mDrawer != null) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }
}
