package com.example.pubnubapplication;

import static com.pubnub.api.enums.PNOperationType.PNHeartbeatOperation;
import static com.pubnub.api.enums.PNOperationType.PNSubscribeOperation;
import static com.pubnub.api.enums.PNOperationType.PNUnsubscribeOperation;
import static com.pubnub.api.enums.PNStatusCategory.PNAccessDeniedCategory;
import static com.pubnub.api.enums.PNStatusCategory.PNConnectedCategory;
import static com.pubnub.api.enums.PNStatusCategory.PNDisconnectedCategory;
import static com.pubnub.api.enums.PNStatusCategory.PNReconnectedCategory;
import static com.pubnub.api.enums.PNStatusCategory.PNUnexpectedDisconnectCategory;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
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

public class PubnubCallback extends SubscribeCallback {
    @Override
    public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

        switch (pnStatus.getOperation()) {
            // combine unsubscribe and subscribe handling for ease of use
            case PNSubscribeOperation:
            case PNUnsubscribeOperation:
                // Note: subscribe statuses never have traditional errors,
                // just categories to represent different issues or successes
                // that occur as part of subscribe
                switch (pnStatus.getCategory()) {
                    case PNConnectedCategory:
                        // No error or issue whatsoever.
                        System.out.println("Connected");
                    case PNReconnectedCategory:
                        // Subscribe temporarily failed but reconnected.
                        // There is no longer any issue.
                        System.out.println("PNReconnectedCategory");
                    case PNDisconnectedCategory:
                        // No error in unsubscribing from everything.
                        System.out.println("PNDisconnectedCategory");
                    case PNUnexpectedDisconnectCategory:
                        // Usually an issue with the internet connection.
                        // This is an error: handle appropriately.
                        System.out.println("PNUnexpectedDisconnectCategory");
                    case PNAccessDeniedCategory:
                        // Access Manager does not allow this client to subscribe to this
                        // channel and channel group configuration. This is
                        // another explicit error.
                        System.out.println("PNAccessDeniedCategory");
                    default:
                        // You can directly specify more errors by creating
                        // explicit cases for other error categories of
                        // `PNStatusCategory` such as `PNTimeoutCategory` or
                        // `PNMalformedFilterExpressionCategory` or
                        // `PNDecryptionErrorCategory`.
                }

            case PNHeartbeatOperation:
                // Heartbeat operations can in fact have errors, so it's important to check first for an error.
                // For more information on how to configure heartbeat notifications through the status
                // PNObjectEventListener callback, refer to
                // /docs/sdks/android/api-reference/configuration#configuration_basic_usage
                if (pnStatus.isError()) {
                    // There was an error with the heartbeat operation, handle here
                } else {
                    // heartbeat operation was successful
                }
            default: {
                // Encountered unknown status type
            }
        }
    }

    @Override
    public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
        System.out.println(pnMessageResult.getMessage());
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
