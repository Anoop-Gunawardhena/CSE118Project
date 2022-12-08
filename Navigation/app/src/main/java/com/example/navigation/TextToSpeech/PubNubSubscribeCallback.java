package com.example.navigation.TextToSpeech;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
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

public class PubNubSubscribeCallback extends SubscribeCallback {

    @Override
    public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
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
    public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
        System.out.println();
        if (message.getMessage().toString().equals("\"safe to cross\"")) {
            //TODO: Rick should activate the Walking straight feature here
//            receivedCallback.message(message.getMessage().toString());
            System.out.println("Safe to cross");
            Speaker.speak("There are no Cars detected, you can now cross the street");
        } else if (message.getMessage().toString().equals("\"wait\"")) {
            Speaker.speak("There are cars approaching, you have to wait");
        }
    }

    @Override
    public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

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
}
