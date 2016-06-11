package me.mikecasper.musicvoice.onboarding;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import me.mikecasper.musicvoice.MainActivity;
import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.login.events.GetUserEvent;
import me.mikecasper.musicvoice.onboarding.events.ScrollLeftEvent;
import me.mikecasper.musicvoice.onboarding.events.ScrollRightEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;

public class OnboardingActivity extends AppCompatActivity {

    private static final int[] DARK_COLORS = new int[] { R.color.first_onboarding_view_dark, R.color.second_onboarding_view_dark, R.color.third_onboarding_view_dark };

    private TabLayout mTabs;
    private ViewPager mViewPager;
    private IEventManager mEventManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEventManager = EventManagerProvider.getInstance(this);

        OnboardingPagerAdapter pagerAdapter = new OnboardingPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);

        mTabs = (TabLayout) findViewById(R.id.onboarding_tabs);
        mTabs.setupWithViewPager(mViewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(OnboardingActivity.this, DARK_COLORS[0]));
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        for (int i = 0; i < mTabs.getTabCount(); i++) {
            mTabs.getTabAt(i).setIcon(R.drawable.tab_indicator);
        }

        LinearLayout tabContainer = (LinearLayout) mTabs.getChildAt(0);

        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            tabContainer.getChildAt(i).setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEventManager.register(this);
    }

    @Override
    protected void onPause() {
        mEventManager.unregister(this);

        super.onPause();
    }

    @Subscribe
    public void onScrollLeft(ScrollLeftEvent event) {
        int currentPosition = mViewPager.getCurrentItem();

        if (currentPosition != 0) {
            mViewPager.setCurrentItem(currentPosition - 1, true);
        }
    }

    @Subscribe
    public void onScrollRight(ScrollRightEvent event) {
        int currentPosition = mViewPager.getCurrentItem();

        if (currentPosition != OnboardingPagerAdapter.FRAGMENT_COUNT - 1) {
            mViewPager.setCurrentItem(currentPosition + 1, true);
        } else {
            moveToMainActivity();
        }
    }

    public void skipOnboarding(View view) {
        moveToMainActivity();
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mEventManager = null;
        mViewPager = null;
        mTabs = null;

        ((MusicVoiceApplication) getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
