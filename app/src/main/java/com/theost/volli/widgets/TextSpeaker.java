package com.theost.volli.widgets;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.theost.volli.R;
import com.theost.volli.utils.DisplayUtils;

import java.util.Locale;

public class TextSpeaker {

    private TextToSpeech textToSpeech;
    private boolean ttsEnabled;

    public TextSpeaker(Context context) {
        textToSpeech = new TextToSpeech(context, initStatus -> {
            if (initStatus == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("RU", "ru"));
                textToSpeech.setPitch(1.1f);
                textToSpeech.setSpeechRate(0.9f);
                ttsEnabled = true;
            } else if (initStatus == TextToSpeech.ERROR) {
                DisplayUtils.showToast(context, R.string.voice_speaker_error);
                ttsEnabled = false;
            }
        });
    }

    public void speak(String text) {
        if (ttsEnabled) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id");
        }
    }

    public void speakAfter(String text) {
        if (ttsEnabled) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "id");
        }
    }

    public void stop() {
        if (ttsEnabled) {
            textToSpeech.stop();
        }
    }

    public boolean isInitialized() {
        return ttsEnabled;
    }

    public void setListener(UtteranceProgressListener speakListener) {
        textToSpeech.setOnUtteranceProgressListener(speakListener);
    }

}
