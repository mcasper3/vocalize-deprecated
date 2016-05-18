package me.mikecasper.musicvoice.services.voicerecognition;

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.api.responses.PlaylistResponse;
import me.mikecasper.musicvoice.models.Playlist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.PauseMusicEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.PlayMusicEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.util.Logger;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class PocketSphinxVoiceRecognizer implements RecognitionListener, IVoiceRecognizer {

    private static final String TAG = "PocketSphinxVoiceRecognizer";

    private static final String ENGLISH_US = "en-us-ptm";
    private static final String ENGLISH_US_DICT = "cmudict-en-us.dict";
    private static final String ALL_PHONE = "-allphone_ci";
    private static final String MAIN_SEARCH = "main";
    private static final String KEYPHRASE = "music voice listen up";
    private static final String COMMAND_SEARCH = "commands";
    private static final String TRACK_SEARCH = "tracks";
    private static final String PLAYLIST_SEARCH = "playlists";

    // Basic commands
    private static final String SKIP = "skip to next song";
    private static final String PREVIOUS = "play previous song";
    private static final String PAUSE = "pause music";
    private static final String RESUME = "resume playback";

    private static final float KEYWORD_THRESHOLD = 1f;

    private IEventManager mEventManager;
    private SpeechRecognizer mRecognizer;
    private Context mContext;
    private boolean mShouldAddTrackGrammarFile;
    private boolean mShouldAddPlaylistGrammarFile;
    private boolean mHasTracksSearch;
    private File mTrackFile;
    private File mPlaylistFile;
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
        mRecognizer.startListening(MAIN_SEARCH);
    }

    public void stopListening() {
        mRecognizer.stop();
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
            mRecognizer.startListening(searchName);
        } else {
            // TODO why is this the same?
            mRecognizer.startListening(searchName);
        }
    }

/*
    @Subscribe
    public void onPlaylistsObtained(PlaylistResponse response) {
        setUpRecognizer();

        List<Playlist> playlists = response.getPlaylists();

        List<String> names = new ArrayList<>(playlists.size());

        for (Playlist playlist : playlists) {
            names.add(playlist.getName());
        }

        File file = createGrammarFile(names, R.array.playlist_grammar_info);

        if (file != null) {
            if (mRecognizer != null) {
                mRecognizer.addGrammarSearch(PLAYLIST_SEARCH, file);
            } else {
                mPlaylistFile = file;
                mShouldAddPlaylistGrammarFile = true;
            }
        }
    }
    */

    public void addTrackGrammarFile(List<Track> tracks) {
        List<String> names = new ArrayList<>(tracks.size());

        for (Track track : tracks) {
            names.add(track.getName());
        }

        File file = createGrammarFile(names, R.array.track_grammar_info);

        if (file != null) {
            if (mRecognizer != null) {
                if (mHasTracksSearch) {
                    setUpRecognizer();
                    mShouldAddTrackGrammarFile = true;
                    mTrackFile = file;
                } else {
                    mHasTracksSearch = true;
                    mRecognizer.addGrammarSearch(TRACK_SEARCH, file);
                }
            } else {
                mTrackFile = file;
                mShouldAddTrackGrammarFile = true;
            }
        }
    }

    private File createGrammarFile(List<String> names, int infoArrayId) {
        String[] info = mContext.getResources().getStringArray(infoArrayId);
        String fileName = info[0];
        String pluralString = info[1];
        String singularString = info[2];
        String key = info[3];

        String grammarHeader = mContext.getString(R.string.grammar_file_header, pluralString, singularString);
        String grammarEnding = mContext.getString(R.string.grammar_file_ending, pluralString, singularString);

        StringBuilder fileContent = new StringBuilder(grammarHeader);

        for (String name : names) {
            fileContent.append("(play ");
            fileContent.append(key);
            fileContent.append(" ");
            fileContent.append(name.toLowerCase());
            fileContent.append(") |\r\n");
        }

        fileContent.deleteCharAt(fileContent.length() - 3);
        fileContent.append(";");
        fileContent.append(grammarEnding);

        File file = null;

        FileOutputStream stream = null;

        try {
            Assets assets = new Assets(mContext);
            File assetsDir = assets.syncAssets();

            file = new File(assetsDir, fileName);
            stream = new FileOutputStream(file);
            stream.write(fileContent.toString().getBytes());
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage(), e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Logger.e(TAG, "Something has gone horribly wrong", e);
                }
            }
        }

        return file;
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
