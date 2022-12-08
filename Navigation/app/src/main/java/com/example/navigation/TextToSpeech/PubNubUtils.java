package com.example.navigation.TextToSpeech;

import android.os.Message;
import android.security.identity.MessageDecryptionException;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PubNubUtils {
    public static PubNubUtils instance = new PubNubUtils();
    private PNConfiguration pnConfiguration;
    private PubNub pubNub;

    public static PubNubUtils getInstance() {
        return instance;
    }

    public PubNubUtils() {
        try {
            pnConfiguration = new PNConfiguration("myUniqueId");
            pnConfiguration.setSubscribeKey("sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028");
            pnConfiguration.setPublishKey("pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9");
            pubNub = new PubNub(pnConfiguration);
            pubNub.addListener(new PubNubSubscribeCallback());
            pubNub.subscribe()
                    .channels(Arrays.asList("Channel-Barcelona")) // subscribe to channels
                    .execute();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void publish(String message) {
        pubNub.publish()
                .message(Arrays.asList(message))
                .channel("Channel-Barcelona")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                        System.out.println("Success sending: " + result);
                        System.out.println(status);
                    }
                });
    }


}
