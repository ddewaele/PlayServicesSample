package com.example.simplemapproject;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.example.simplemapproject.utl.Utils;

/**
 * 
 * IntentService responsible for capturing the incoming locations from the LocationManager.
 * 
 * @author Davy
 *
 */
public class MyLocationService extends IntentService {

	public MyLocationService() {
		super("com.ecs.myLocationService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("MyLocationService - onHandleIntent called with " + intent);
		if (Constants.INTENT_ACTION_LOCATION_UPDATED.equals(intent.getAction())) {
			Location location = (Location) intent.getExtras().get(Constants.INTENT_EXTRA_LOCATION);
			System.out.println("Found location = " + location);
			Utils.writeSerializedLocationToDisk(Constants.FILENAME_LOCATION,location, getApplicationContext());

			
			Intent uiIntent = new Intent();
			uiIntent.setAction(Constants.INTENT_ACTION_LOCATION_UPDATE_MAP);
			uiIntent.addCategory(Intent.CATEGORY_DEFAULT);
			uiIntent.putExtra(Constants.INTENT_EXTRA_LOCATION, Utils.convertLocationToLatLng(location));
			sendBroadcast(uiIntent);			
		}
	}


}
