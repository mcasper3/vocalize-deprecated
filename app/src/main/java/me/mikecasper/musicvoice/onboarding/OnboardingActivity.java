package me.mikecasper.musicvoice.onboarding;

import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.LinearLayout;

import me.mikecasper.musicvoice.R;

public class OnboardingActivity extends AppCompatActivity {

    private static final int[] DARK_COLORS = new int[] { R.color.dark_gold, R.color.dark_blue, R.color.dark_orange };

    private TabLayout mTabs;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OnboardingPagerAdapter pagerAdapter = new OnboardingPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(pagerAdapter);

        mTabs = (TabLayout) findViewById(R.id.onboarding_tabs);
        mTabs.setupWithViewPager(viewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(OnboardingActivity.this, DARK_COLORS[0]));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabs.getTabAt(position).select();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int darkColor = DARK_COLORS[position];
                    getWindow().setStatusBarColor(ContextCompat.getColor(OnboardingActivity.this, darkColor));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        for (int i = 0; i < mTabs.getTabCount(); i++) {
            mTabs.getTabAt(i).setIcon(R.drawable.tab_indicator);
        }

        LinearLayout tabContainer = (LinearLayout) mTabs.getChildAt(0);

        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            tabContainer.getChildAt(i).setClickable(false);
        }
    }
}
