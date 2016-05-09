package me.mikecasper.musicvoice.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.mikecasper.musicvoice.R;

public class OnboardingPagerAdapter extends FragmentPagerAdapter {

    private static final int[] COLORS = new int[] { R.color.first_onboarding_view, R.color.second_onboarding_view, R.color.third_onboarding_view };
    private static final int[] TEXT_OPTIONS = new int[] { R.string.select_view_layout };
    private static final int[] IMAGES = new int[] { R.drawable.ic_phone };

    public OnboardingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new OnboardingFragment();

        Bundle args = new Bundle();
        args.putInt(OnboardingFragment.BACKGROUND_COLOR, COLORS[position]);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
