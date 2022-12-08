package com.example.navigation.TextToSpeech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.Locale;

//Helper class to listen to input and convert it to text
public class Mic {

    public static Mic instance = new Mic();
    private static SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent;

    public static Mic getInstance() {
        return instance;
    }

    public Mic() {
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
    }

    public void listen() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void setSpeechRecognizer(SpeechRecognizer speechRecognizer) {
        if (Mic.speechRecognizer == null) {
            Mic.speechRecognizer = speechRecognizer;
        }
    }
}









