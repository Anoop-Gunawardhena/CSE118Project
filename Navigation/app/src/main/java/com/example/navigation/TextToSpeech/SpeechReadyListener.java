package com.example.navigation.TextToSpeech;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechReadyListener implements RecognitionListener {

    private Context context;

    public SpeechReadyListener(Context context) {
        this.context = context;
    }
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
        System.out.println("Result");
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        System.out.println(data.get(0));
        if (!data.isEmpty()) {
            System.out.println("result " + data.get(0));
            if (data.get(0).contains("yes") || data.get(0).contains("cross")) {
                System.out.println("Activate PI");
                Toast.makeText(this.context, "User said yes", Toast.LENGTH_LONG).show();
                Speaker.speak("Okay, I am now listening for approaching cars");
                PubNubUtils.getInstance().publish("record");
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        System.out.println("Result");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}
