package com.example.navigation.TextToSpeech;

import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSOnInitListener implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;

    public TTSOnInitListener(TextToSpeech textToSpeech) {
        this.textToSpeech = textToSpeech;
    }
    @Override
    public void onInit(int ttsStatus) {
        if (ttsStatus != TextToSpeech.ERROR) {
            textToSpeech.setLanguage(Locale.US);
        } else {
            System.out.println(ttsStatus);
        }
    }
}
