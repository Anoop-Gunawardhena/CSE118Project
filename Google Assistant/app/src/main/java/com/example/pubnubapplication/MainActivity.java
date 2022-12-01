package com.example.pubnubapplication;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.speech.tts.TextToSpeech;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pubnubapplication.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

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

import java.util.Locale;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private PNConfiguration pnConfiguration;
    private PubNub pubNub;

    TextToSpeech tts1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            pnConfiguration = new PNConfiguration("myUniqueId");
            pnConfiguration.setSubscribeKey("sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028");
            pnConfiguration.setPublishKey("pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9");
            pubNub = new PubNub(pnConfiguration);



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
                    System.out.println(message.getMessage());
                    // Let the Google Assistant ouput the message as soon as it arrives
                    // Pseudo code: GoogleAssistant.speak(message.getMessage()
                    tts1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int ttsStatus) {
                            if(ttsStatus != TextToSpeech.ERROR) {
                                tts1.setLanguage(Locale.US);
                                System.out.println("I have initialized!");
                                String toSpeak = message.getMessage().toString();
                                tts1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                            else
                            {
                                System.out.println(ttsStatus);
                            }
                        }
                        public void onPause(){
                            if(tts1 != null){
                                tts1.stop();
                                tts1.shutdown();
                            }
                        }
                    }, "com.google.android.tts");
                    //String toSpeak = message.getMessage().toString();
                    //tts1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
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

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pubNub.publish()
                        .message(Arrays.asList("Hello Rick"))
                        .channel("Channel-Barcelona")
                        .async(new PNCallback<PNPublishResult>() {
                            @Override
                            public void onResponse(PNPublishResult result, PNStatus status) {
                                // handle publish result, status always present, result if successful
                                // status.isError to see if error happened
                                System.out.println("Success sending" + result);
                                System.out.println(status);
                            }
                        });
            }
        });
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