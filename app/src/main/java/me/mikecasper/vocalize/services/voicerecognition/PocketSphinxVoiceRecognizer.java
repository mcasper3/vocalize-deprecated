package me.mikecasper.vocalize.services.voicerecognition;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.PauseMusicEvent;
import me.mikecasper.vocalize.services.musicplayer.events.PlayMusicEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.vocalize.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.vocalize.util.Logger;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class PocketSphinxVoiceRecognizer implements RecognitionListener, IVoiceRecognizer {

    private static final String TAG = "SphinxVoiceRecognizer";

    private static final String ENGLISH_US = "en-us-ptm";
    private static final String ENGLISH_US_DICT = "cmudict-en-us.dict";
    private static final String ALL_PHONE = "-allphone_ci";
    private static final String MAIN_SEARCH = "main";
    private static final String KEYPHRASE = "vocalize hear me out";
    private static final String COMMAND_SEARCH = "commands";

    // Basic commands
    private static final String SKIP = "skip to next song";
    private static final String PREVIOUS = "play previous song";
    private static final String PAUSE = "pause music";
    private static final String RESUME = "resume playback";
    private static final String SHUFFLE = "shuffle play";

    private static final float KEYWORD_THRESHOLD = 1e-20f;

    private IEventManager mEventManager;
    private SpeechRecognizer mRecognizer;
    private Context mContext;
    private SetUpRecognizerTask mTask;

    public PocketSphinxVoiceRecognizer(Context context) {
        mEventManager = EventManagerProvider.getInstance(context);
        mContext = context;

        setUpRecognizer();
    }

    private void setUpRecognizer() {
        if (mTask != null && (mTask.getStatus() == AsyncTask.Status.PENDING || mTask.getStatus() == AsyncTask.Status.RUNNING)) {
            mTask.cancel(false);
        }

        mTask = new SetUpRecognizerTask();
        mTask.execute(mContext);
    }

    public void startListening() {
        Log.i(TAG, "started");
        if (mRecognizer != null) {
            mRecognizer.startListening(MAIN_SEARCH);
        }
    }

    public void stopListening() {
        Log.i(TAG, "stopped");
        if (mRecognizer != null) {
            mRecognizer.stop();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        if (!mRecognizer.getSearchName().equals(MAIN_SEARCH)) {
            switchSearch(MAIN_SEARCH);
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        Logger.d(TAG, text);

        if (text.equals(KEYPHRASE)) {
            switchSearch(COMMAND_SEARCH);
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();

            switch (text) {
                case SKIP:
                    Logger.d(TAG, "Skipping song");
                    mEventManager.postEvent(new SkipForwardEvent());
                    break;
                case PREVIOUS:
                    Logger.d(TAG, "Skipping backward");
                    mEventManager.postEvent(new SkipBackwardEvent());
                    break;
                case PAUSE:
                    Logger.d(TAG, "Pausing Music");
                    mEventManager.postEvent(new PauseMusicEvent());
                    break;
                case RESUME:
                    Logger.d(TAG, "Resuming Music");
                    mEventManager.postEvent(new PlayMusicEvent());
                    break;
            }
        }
    }

    @Override
    public void onError(Exception e) {
        Logger.e(TAG, e.getMessage(), e);
        // TODO maybe a "I didn't catch that" toast?

        Toast.makeText(mContext, "I didn't catch that", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout() {
        Logger.d(TAG, "In on timeout");

        switchSearch(MAIN_SEARCH);
    }

    private void switchSearch(String searchName) {
        mRecognizer.stop();

        // TODO figure out if this needs to change with two grammars
        if (searchName.equals(MAIN_SEARCH)) {
            mRecognizer.startListening(searchName, 4000);
        } else {
            // TODO why is this the same?
            mRecognizer.startListening(searchName);
        }
    }

    private class SetUpRecognizerTask extends AsyncTask<Context, Void, SpeechRecognizer> {

        @Override
        protected SpeechRecognizer doInBackground(Context... params) {
            Context context = params[0];
            SpeechRecognizer recognizer = null;

            try {
                Assets assets = new Assets(context);
                File assetDir = assets.syncAssets();

                recognizer = defaultSetup()
                        .setAcousticModel(new File(assetDir, ENGLISH_US))
                        .setDictionary(new File(assetDir, ENGLISH_US_DICT))
                        .setKeywordThreshold(KEYWORD_THRESHOLD)
                        .setBoolean(ALL_PHONE, true)
                        .getRecognizer();

                recognizer.addListener(PocketSphinxVoiceRecognizer.this);

                recognizer.addKeyphraseSearch(MAIN_SEARCH, KEYPHRASE);

                File commandGrammar = new File(assetDir, "commands.gram");
                recognizer.addGrammarSearch(COMMAND_SEARCH, commandGrammar);
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }

            return recognizer;
        }

        @Override
        protected void onPostExecute(SpeechRecognizer speechRecognizer) {
            super.onPostExecute(speechRecognizer);

            if (speechRecognizer != null) {
                mRecognizer = speechRecognizer;

                /*if (mShouldAddPlaylistGrammarFile) {
                    //mRecognizer.addGrammarSearch(PLAYLIST_SEARCH, mPlaylistFile);
                    mPlaylistFile = null;
                    mShouldAddPlaylistGrammarFile = false;
                }

                if (mShouldAddTrackGrammarFile) {
                    mRecognizer.addGrammarSearch(TRACK_SEARCH, mTrackFile);
                    mTrackFile = null;
                    mShouldAddTrackGrammarFile = false;
                }*/
            }
        }
    }
}
