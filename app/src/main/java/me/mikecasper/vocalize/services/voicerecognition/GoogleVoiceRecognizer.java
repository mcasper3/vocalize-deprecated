package me.mikecasper.vocalize.services.voicerecognition;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import edu.cmu.pocketsphinx.Hypothesis;

public class GoogleVoiceRecognizer implements IVoiceRecognizer, RecognitionListener {

    private SpeechRecognizer mSpeechRecognizer;
    private Context mContext;

    public GoogleVoiceRecognizer(Context context) {
        mContext = context;

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(this);
    }

    @Override
    public void startListening() {
//        mSpeechRecognizer.startListening();
    }

    @Override
    public void stopListening() {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
//        if (!mRecognizer.getSearchName().equals(MAIN_SEARCH)) {
//            switchSearch(MAIN_SEARCH);
//        }
    }

    //@Override
    public void onPartialResult(Hypothesis hypothesis) {
        /*if (hypothesis == null) {
            return;
        }

        String text = hypothesis.getHypstr();
        Logger.d(TAG, text);

        if (text.equals(KEYPHRASE)) {
            switchSearch(COMMAND_SEARCH);
        }*/
    }

    @Override
    public void onError(int error) {
        //Logger.e(TAG, "Error " + error);
        // TODO maybe a "I didn't catch that" toast?

        Toast.makeText(mContext, "I didn't catch that", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onResults(Bundle results) {
        /*if (hypothesis != null) {
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
        }*/
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
