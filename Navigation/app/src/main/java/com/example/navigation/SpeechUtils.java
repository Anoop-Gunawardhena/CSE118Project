package com.example.navigation;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public abstract class SpeechUtils {

    TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;

    void output(Context context, String toSpeak) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int ttsStatus) {
                if (ttsStatus != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    //System.out.println("I have initialized!");
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    System.out.println(ttsStatus);
                }
            }

            public void onPause() {
                if (tts != null) {
                    tts.stop();
                    tts.shutdown();
                }
            }
        }, "com.google.android.tts");
    }

    void openMic(){
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(speechRecognizerIntent);
    }

}
