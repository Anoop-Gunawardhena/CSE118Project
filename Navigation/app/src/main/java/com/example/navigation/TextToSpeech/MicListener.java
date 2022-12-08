package com.example.navigation.TextToSpeech;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public class MicListener implements RecognitionListener {


    @Override
    public void onReadyForSpeech(Bundle bundle) {
        System.out.println("ready");
    }

    @Override
    public void onBeginningOfSpeech() {
        System.out.println("begin");
    }

    @Override
    public void onRmsChanged(float v) {
        System.out.println("changed");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        System.out.println("Received");
    }

    @Override
    public void onEndOfSpeech() {
        System.out.println("End");
    }

    @Override
    public void onError(int i) {
        System.out.println("Error " + i);
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        System.out.println("result " + data.get(0));
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        System.out.println("partial result");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}
