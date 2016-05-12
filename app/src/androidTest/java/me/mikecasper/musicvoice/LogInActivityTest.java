package me.mikecasper.musicvoice;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import me.mikecasper.musicvoice.login.LogInActivity;

@RunWith(AndroidJUnit4.class)
public class LogInActivityTest {

    @Rule
    public IntentsTestRule<LogInActivity> mActivityTestRule = new IntentsTestRule<>(LogInActivity.class);

    @After
    public void tearDown() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getContext());
        sharedPreferences.edit().clear().apply();
    }

    @Test
    public void loginButtonIsVisible() {
        onView(withId(R.id.log_in_button)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.log_in_button)).check(matches(isCompletelyDisplayed()));
    }

}