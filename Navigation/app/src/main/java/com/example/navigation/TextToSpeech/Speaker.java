package com.example.navigation.TextToSpeech;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class Speaker {
    static TextToSpeech textToSpeech;
    private static Context context;

    public static TextToSpeech speak(String text){
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        return textToSpeech;
    }

    public static void setContext(Context context) {
        Speaker.context = context;
    }
}
