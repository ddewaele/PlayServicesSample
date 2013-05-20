package com.example.simplemapproject;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

public class MyLocationService extends IntentService {

	public MyLocationService() {
		super("com.ecs.myLocationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("MyLocationService - onHandleIntent called with " + intent);
		if (Constants.INTENT_ACTION_LOCATION_UPDATED.equals(intent.getAction())) {
			Location location = (Location) intent.getExtras().get("com.google.android.location.LOCATION");
			System.out.println("Found location = " + location);
		}
	}

}
