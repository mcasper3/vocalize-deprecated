package me.mikecasper.musicvoice.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.mikecasper.musicvoice.R;

public class OnboardingPagerAdapter extends FragmentPagerAdapter {

    public static final int FRAGMENT_COUNT = 2;

    private static final int[] COLORS = new int[] { R.color.first_onboarding_view, R.color.second_onboarding_view, R.color.third_onboarding_view };
    private static final int[] TEXT_OPTIONS = new int[] { R.string.settings_onboarding, R.string.voice_onboarding };
    private static final int[] VOICE_COMMANDS = new int[] { R.string.play_hint, R.string.pause_hint, R.string.skip_forward_hint, R.string.skip_backward_hint };
    private static final int[] IMAGES = new int[] { R.drawable.guy_headphones, R.drawable.girl_headphones };
    private static final int[] LEFT_OPTIONS = new int[] { R.string.left, R.string.back, R.string.back };
    private static final int[] RIGHT_OPTIONS = new int[] { R.string.right, R.string.next, R.string.ok };

    public OnboardingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new OnboardingFragment();

        Bundle args = new Bundle();
        args.putInt(OnboardingFragment.BACKGROUND_COLOR, COLORS[position]);
        args.putInt(OnboardingFragment.LEFT_OPTION, LEFT_OPTIONS[position]);
        args.putInt(OnboardingFragment.RIGHT_OPTION, RIGHT_OPTIONS[position]);
        args.putInt(OnboardingFragment.TEXT_ID, TEXT_OPTIONS[position]);
        args.putInt(OnboardingFragment.IMAGE_ID, IMAGES[position]);

        if (position == FRAGMENT_COUNT - 1) {
            args.putIntArray(OnboardingFragment.VOICE_COMMANDS, VOICE_COMMANDS);
        }

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }
}
