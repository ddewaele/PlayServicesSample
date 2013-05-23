package com.example.simplemapproject;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.simplemapproject.utl.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BasicMapActivity extends FragmentActivity implements OnConnectionFailedListener {

	private static final String START_ACTIVITY_POLLING = "START_ACTIVITY_POLLING";


	private static final int LOCATION_INTENT_CODE = 1;


	private String requestType;
	
    private GoogleMap googleMap;
	private LocationClient locationClient;
	private ActivityRecognitionClient activityRecognitionClient; 
	
	private Button stopLocationPolling;
	private Button startLocationPolling;

	private Button stopActivityPolling;
	private Button startActivityPolling;
	
	private Button clearAllMarkers;
	private Button selectLastMarker;

	private boolean activityPollingInProgress;
	
	private List<Marker> markers = new ArrayList<Marker>();
	
	private ManualLocationResponseReceiver receiver; 
	
	
	/**
	 * 
	 * This is the receiver that will update the map.
	 * It receives via the intent
	 * 		1. The LatLng object
	 * 		2. A timestamp when the location was recorded.
	 * 
	 * 
	 */
	private BroadcastReceiver updateMapReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (Constants.INTENT_ACTION_LOCATION_UPDATE_MAP.equals(intent.getAction())) {
				LatLng latLng = (LatLng) intent.getExtras().get(Constants.INTENT_EXTRA_LATLNG);
				Location location = (Location) intent.getExtras().get(Constants.INTENT_EXTRA_LOCATION);
				System.out.println("Adding LatLng " + latLng + " to map");
				//clearAllMarkers();
				//addMarkerToMap(latLng);
				addLocationToMap(location);
				
			} else {
				System.out.println("Unknown action received " + intent.getAction());
			}
		}

	};

	

	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_demo);
        
        System.out.println("onCreate called");
        
        
        
        this.activityPollingInProgress=false;
        
        int gpsExist = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        locationClient = new LocationClient(this,new  GooglePlayServicesClient.ConnectionCallbacks() {
    		
    		@Override
    		public void onDisconnected() {
    			System.out.println("onDisconnected");
    			
    		}
    		
    		@Override
    		public void onConnected(Bundle bundle) {
    			startLocationUpdates();
    		}

    	},this);
        
        activityRecognitionClient = new ActivityRecognitionClient(this, new  GooglePlayServicesClient.ConnectionCallbacks() {
    		
    		@Override
    		public void onDisconnected() {
    			System.out.println("onDisconnected");
    			activityPollingInProgress=false;
    			activityRecognitionClient = null;
    			
    		}
    		
    		@Override
    		public void onConnected(Bundle bundle) {
    			startActivityRecognitionUpdates();
    		}

    	},this);

        
        stopLocationPolling = (Button) findViewById(R.id.stopLocationPolling);
        stopLocationPolling.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("stopLocationPolling");
				locationClient.disconnect();
			}
		});
        
        startLocationPolling = (Button) findViewById(R.id.startLocationPolling);
        startLocationPolling.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("startLocationPolling");
				locationClient.connect();
				
			}
		});
        
        
        stopActivityPolling = (Button) findViewById(R.id.stopActivityPolling);
        stopActivityPolling.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("stopActivityPolling");
				locationClient.disconnect();
			}
		});
        
        startActivityPolling = (Button) findViewById(R.id.startActivityPolling);
        startActivityPolling.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!activityPollingInProgress) {
					System.out.println("startActivityPolling");
					activityPollingInProgress=true;
					activityRecognitionClient.connect();
				} else {
					System.out.println("Request already underway");
				}
			}
		});
        
        clearAllMarkers = (Button) findViewById(R.id.clearAllMarkers);
        clearAllMarkers.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("clearAllMarkers");
				clearAllMarkers();
			}
		});
        
        selectLastMarker = (Button) findViewById(R.id.selectLastMarker);
        selectLastMarker.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("selectLastMarker");
				selectLastMarker();
			}
		});        
        
        setUpMapIfNeeded();
        
        locationClient.connect();
        activityRecognitionClient.connect();
        
        //retrieveLocationUsingPendingIntent();


    }

	private void registerUpdateMapReceiver() {
		IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_LOCATION_UPDATE_MAP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(updateMapReceiver, filter);		
	}

	private void registerManualLocationReceiver() {
		IntentFilter filter = new IntentFilter(Constants.INTENT_ACTION_MANUAL_LOCATION_UPDATED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ManualLocationResponseReceiver();
        registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		unregisterReceiver(updateMapReceiver);
		super.onPause();
	};

    @Override
    protected void onResume() {
        setUpMapIfNeeded();
        registerManualLocationReceiver();
        registerUpdateMapReceiver();
        super.onResume();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
				System.out.println("Clicked on map with LatLng : " + latLng);
				
				String title = "Manual location";
				String snippet = "spotted you here at " + Utils.parseDate(BasicMapActivity.this);
				
				addMarkerToMap(latLng,title,snippet);
				
    			Intent intent = new Intent();
    			intent.setAction(Constants.INTENT_ACTION_MANUAL_LOCATION_UPDATED);
    			intent.addCategory(Intent.CATEGORY_DEFAULT);
    			intent.putExtra(Constants.INTENT_EXTRA_LOCATION, Utils.convertLatLngToLocation(latLng));
    			sendBroadcast(intent);
    			
			}

		});
        
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				System.out.println("Clicked on marker " + marker);
				int indexOf = markers.indexOf(marker);
				Marker markerFromList = markers.get(indexOf);
				//markerFromList.remove();
				return false;
			}
		});
        
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				System.out.println("Clicked on infoWindow" + marker);
				int indexOf = markers.indexOf(marker);
				Marker markerFromList = markers.get(indexOf);
				System.out.println(markerFromList);
				marker.remove();
			}
		});
        
        googleMap.setMyLocationEnabled(true);

    }
    
    
    private void clearAllMarkers() {
    	googleMap.clear();
    	markers.clear();
    	
    }
	private void addMarkerToMap(LatLng latLng,String title,String snippet) {
		Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
															 .title(title)
															 .snippet(snippet));
		
		markers.add(marker);
		navigateToPoint(latLng);
	}
	

	private void addLocationToMap(Location location) {
		StringBuffer snippet = new StringBuffer("spotted you here at " + Utils.parseDate(location.getTime(), BasicMapActivity.this));
		snippet.append("\n");
		
		snippet.append("Accuracy : ");
		snippet.append(location.getAccuracy());
		snippet.append("\n");
		snippet.append("Speed : ");
		snippet.append(location.getSpeed());
		snippet.append("\n");
		snippet.append("Provider: ");
		snippet.append(location.getProvider());
		snippet.append("\n");
		
		String title = "Spotted !";
		LatLng latLng = Utils.convertLocationToLatLng(location);
		addMarkerToMap(latLng, title, snippet.toString());
	}	
	
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
       	googleMap.animateCamera(update, callback);
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    
    private void navigateToPoint(LatLng latLng) {
    	CameraPosition position =
                new CameraPosition.Builder().target(latLng)
                        .zoom(15.5f)
                        .bearing(0)
                        .tilt(25)
                        .build();
    	
    	changeCamera(CameraUpdateFactory.newCameraPosition(position)); 
    }
	

	
    private void retrieveLocationUsingPendingIntent() {
    	locationClient.connect();
    }
    
    private void selectFirstMarker() {
    	
    	Marker marker = this.markers.get(0);

    	if(marker != null)
    	{
    	    marker.showInfoWindow();
    	}
    }


    private void selectLastMarker() {
    	
    	Marker marker = this.markers.get(this.markers.size()-1);

    	if(marker != null)
    	{
    	    marker.showInfoWindow();
    	}
    }
    
    private void retrieveLocationUsingListener() {
    	locationClient.connect();
    	Location lastLocation = locationClient.getLastLocation();
    	
    	System.out.println("Found location " + lastLocation);
    	
    	LocationRequest locationRequest = LocationRequest.create().setInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    	
    	
    	locationClient.requestLocationUpdates(locationRequest, locationListener);
    	
    }
    
    private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			System.out.println("Found location " + location);
		}
		

	};

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		System.out.println("onConnectionFailed called");
	}
	
	private void startActivityPolling() {
		requestType = START_ACTIVITY_POLLING;
		
	}
	
	private void startActivityRecognitionUpdates() {
		System.out.println("onConnected");
		Intent intent = new Intent(BasicMapActivity.this,MyActivityService.class);
		intent.setAction(Constants.INTENT_ACTION_RECOGNITION_CHANGE);
		PendingIntent pi = PendingIntent.getService(BasicMapActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		activityRecognitionClient.requestActivityUpdates(5000, pi);
		
		activityPollingInProgress=false;
		activityRecognitionClient.disconnect();
	}
	
	private void startLocationUpdates() {
		System.out.println("onConnected");
    	Intent intent = new Intent(Constants.INTENT_ACTION_LOCATION_UPDATED);
    	PendingIntent pendingIntent = PendingIntent.getService(BasicMapActivity.this, LOCATION_INTENT_CODE, intent, 0);
    	LocationRequest locationRequest = LocationRequest.create().setInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    	locationClient.requestLocationUpdates(locationRequest, pendingIntent);
	}
	

}
