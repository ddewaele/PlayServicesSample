package com.example.simplemapproject;

import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.simplemapproject.utl.Utils;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MyActivityService extends IntentService {

	public MyActivityService() {
		super("com.ecs.myActivityService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("MyActivityService - onHandleIntent called with " + intent);
		if (Constants.INTENT_ACTION_RECOGNITION_CHANGE.equals(intent.getAction())) {
			//System.out.println("Found change = " + intent);
			ActivityRecognitionResult activityRecognitionResult = (ActivityRecognitionResult) intent.getExtras().get("com.google.android.location.internal.EXTRA_ACTIVITY_RESULT");
			DetectedActivity mostProbableActivity = activityRecognitionResult.getMostProbableActivity();
			
			dumpDetectedActivity(getApplicationContext(),mostProbableActivity);
			
			
			List<DetectedActivity> probableActivities = activityRecognitionResult.getProbableActivities();
			
			if(probableActivities!=null && probableActivities.size()>1) {
				Utils.writeToFile(Constants.ACTIVITY_LOCATION, "------------------", getApplicationContext());
				for (DetectedActivity detectedActivity : probableActivities) {
					dumpDetectedActivity(getApplicationContext(),detectedActivity);	
				}
				Utils.writeToFile(Constants.ACTIVITY_LOCATION, "------------------", getApplicationContext());
			}
			
		}
	}
	
	private void dumpDetectedActivity(Context context,DetectedActivity detectedActivity) {
		String activityDump = Utils.parseDate(context) + "|" + getNameFromType(detectedActivity.getType()) + "|" + detectedActivity;
		System.out.println(activityDump);
		Utils.writeToFile(Constants.ACTIVITY_LOCATION, activityDump, context);
	}
	
	 /**
     * Map detected activity types to strings
     *@param activityType The detected activity type
     *@return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

}
