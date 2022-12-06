package com.example.pubnubapplication;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;

import android.speech.tts.TextToSpeech;
//import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pubnubapplication.databinding.ActivityMainBinding;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private PNConfiguration pnConfiguration;
    private PubNub pubNub;

    private SpeechRecognizer speechRecognizer;

    TextToSpeech tts;
    void output(String toSpeak) {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            pnConfiguration = new PNConfiguration("myUniqueId");
            pnConfiguration.setSubscribeKey("sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028");
            pnConfiguration.setPublishKey("pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9");
            pubNub = new PubNub(pnConfiguration);

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    System.out.println("Ready");
                }

                @Override
                public void onBeginningOfSpeech() {
                    System.out.println("Beginning");
                }

                @Override
                public void onRmsChanged(float v) {
                    System.out.println("Changed");
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
                    System.out.println("Error" + i);
                }

                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if (data.get(0).equals("yes")) {
                        System.out.println("User answered \"Yes\"");
                        speechRecognizer.stopListening();
                        pubNub.publish()
                                .message(Arrays.asList("record"))
                                .channel("Channel-Barcelona")
                                .async(new PNCallback<PNPublishResult>() {
                                    @Override
                                    public void onResponse(PNPublishResult result, PNStatus status) {
                                        // handle publish result, status always present, result if successful
                                        // status.isError to see if error happened
                                        System.out.println("Success sending: " + result);
                                        System.out.println(status);
                                    }
                                }); //change yes stuff to send message to pi, create a no thing, implement listening logic from correct message
                    }
                    if(data.get(0).equals("no")){
                        speechRecognizer.stopListening();
                        output("understood");
                    }
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    System.out.println(data.get(0));

                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });

            SubscribeCallback subscribeCallback = new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                        // internet got lost, do some magic and call reconnect when ready
                        pubnub.reconnect();
                    } else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                        // do some magic and call reconnect when ready
                        pubnub.reconnect();
                    } else {
                        System.out.println(status.isError());
                    }
                }

                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    System.out.println(message.getMessage().toString());
                    // Let the Google Assistant ouput the message as soon as it arrives
                    // Pseudo code: GoogleAssistant.speak(message.getMessage()


                    if(message.getMessage().toString().equals("\"crosswalk detected\"")) {
                        //TODO: Move this to the part where Rick detects a crosswalk with his geofence
                        System.out.println("sending message to watch");
                        output("crosswalk detected, would you like to cross?");

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //this runs on the UI thread
                                openMic();
                            }
                        }, 3000);
                    }
                    //Message from the PI
                    else if(message.getMessage().toString().equals("[\"safe to cross\"]")) {
                        //TODO: Rick should activate the Walking straight feature here
                        output("There are no cars detected, you can cross now");
                    }
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {

                }

                @Override
                public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

                }

                @Override
                public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {

                }

                @Override
                public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {

                }

                @Override
                public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

                }

                @Override
                public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

                }

                @Override
                public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

                }
            };

            pubNub.addListener(subscribeCallback);
            pubNub.subscribe()
                    .channels(Arrays.asList("Channel-Barcelona")) // subscribe to channels
                    .execute();

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }


        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}