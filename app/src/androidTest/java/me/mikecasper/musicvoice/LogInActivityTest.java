package me.mikecasper.musicvoice;

import android.os.Parcel;
import android.support.test.espresso.core.deps.guava.eventbus.Subscribe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
import me.mikecasper.musicvoice.models.Image;

@RunWith(AndroidJUnit4.class)
public class LogInActivityTest {

    private Image mImage;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void createImage() {
        mImage = new Image(10, 12, "url");
    }

    @Test
    public void image_ParcelableWriteRead() {


    }

}