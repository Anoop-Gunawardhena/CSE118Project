package com.example.navigation.TextToSpeech;

import android.speech.tts.UtteranceProgressListener;

public class UtteranceListener extends UtteranceProgressListener {

    private UtteranceProgress utteranceProgress;

    public UtteranceListener(UtteranceProgress listenResultCallback){
        this.utteranceProgress = listenResultCallback;
    }

    @Override
    public void onStart(String s) {
        System.out.println("Start");
    }

    @Override
    public void onDone(String s) {
        System.out.println("Done");
        utteranceProgress.onDone(s);
    }

    @Override
    public void onError(String s) {
        System.out.println("Error");
    }
}
