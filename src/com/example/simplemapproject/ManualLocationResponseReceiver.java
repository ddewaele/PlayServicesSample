package com.example.simplemapproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.example.simplemapproject.utl.Utils;

public class ManualLocationResponseReceiver extends BroadcastReceiver {

	@Override
    public void onReceive(Context context, Intent intent) {
		System.out.println("MyLocationService - onHandleIntent called with " + intent);
		if (Constants.INTENT_ACTION_LOCATION_UPDATED.equals(intent.getAction())) {
			Location location = (Location) intent.getExtras().get(Constants.INTENT_EXTRA_LOCATION);
			System.out.println("Found location = " + location);
			Utils.writeSerializedLocationToDisk(location, context);
		} else {
			System.out.println("Unknown action found....");
		}
    }
}