//package com.example.navigation;
//
//import android.content.Context;
//import android.widget.Toast;
//
//import com.google.android.gms.location.Geofence;
//
//public class WalkingStraightGeofenceBroadcastReceiver extends GeofenceBroadcastReceiver {
//
//    /**
//     * MESSAGE PUBLICATION POST-CROSSWALK DETECTED FEATURE
//     *
//     * Publish PubNub message to start Voiceflow workflow if user is proximal to a crosswalk (GEOFENCE_TRANSITION_ENTER is a success)
//     */
//    @Override
//    protected void mapTransition(Context context, int transition) {
//
//        switch (transition) {
//
//            case Geofence.GEOFENCE_TRANSITION_ENTER:
//                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
//                System.out.println("onReceive: Walking Straight GEOFENCE TRANSITION ENTER detected");
//                break;
//
//            case Geofence.GEOFENCE_TRANSITION_EXIT:
//                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
//                System.out.println("onReceive: Walking Straight GEOFENCE TRANSITION EXIT detected. Publishing CROSSWALK_DETECTED message to " + Constants.laurenzChannelName);
//                pubnubMain(Constants.NOT_WALKING_STRAIGHT_DETECTED);    // PUBNUB PUBLICATION
//                break;
//
//            default:
//                Toast.makeText(context, "Walking Straight GEOFENCE_TRANSITION_UNKNOWN", Toast.LENGTH_SHORT).show();
//                System.out.println("onReceive: Walking Straight Geofence transition code " + transition + " detected. Unknown Action. Pass.");
//                break;
//
//        }
//
//    }
//
//}
