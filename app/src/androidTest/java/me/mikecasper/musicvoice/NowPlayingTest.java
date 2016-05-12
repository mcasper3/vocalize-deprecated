package me.mikecasper.musicvoice;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.mikecasper.musicvoice.nowplaying.NowPlayingActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static me.mikecasper.musicvoice.helpers.EspressoTestMatchers.withDrawable;

@RunWith(AndroidJUnit4.class)
public class NowPlayingTest {

    @Rule
    public ActivityTestRule<NowPlayingActivity> mRule = new ActivityTestRule<>(NowPlayingActivity.class, true, false);

    @Test
    public void testPlayButtonChange() {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = new Intent(context, NowPlayingActivity.class);
        intent.putExtra(NowPlayingActivity.IS_PLAYING_MUSIC, true);

        mRule.launchActivity(intent);

        onView(withId(R.id.play_pause_button)).perform(click());
        onView(withId(R.id.play_pause_button)).check(matches(withDrawable(R.drawable.ic_pause)));
    }
}
