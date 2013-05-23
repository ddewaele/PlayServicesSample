package com.example.simplemapproject.utl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

public final class Utils {

	
	public static final Location convertLatLngToLocation(LatLng latLng) {
		
		Location location = new Location("manual");
		location.setLatitude(latLng.latitude);
		location.setLongitude(latLng.longitude);
		return location;
		
	}
	
	public static final LatLng convertLocationToLatLng(Location location) {

		return new LatLng(location.getLatitude(), location.getLongitude());
		
	}
	
	
	
	
	public static final String parseDate(long time,Context ctx) {
		DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.getDefault());
		Date resultdate = new Date(time);
		return sdf.format(resultdate);
	}
	
	public static final void writeToFile(String fileName, String txt,Context context) {
		try {
			
			txt += "\n";
			
		    FileOutputStream fos = context.openFileOutput(fileName,
		            Context.MODE_APPEND | Context.MODE_WORLD_READABLE);
		    fos.write(txt.toString().getBytes());
		    fos.close();
		 
		    String storageState = Environment.getExternalStorageState();
		    
		    if (storageState.equals(Environment.MEDIA_MOUNTED)) {
		        File file = new File(context.getExternalFilesDir(null),
		                fileName);
		        FileOutputStream fos2 = new FileOutputStream(file,true);
		        fos2.write(txt.toString().getBytes());
		        fos2.close();
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}		
	
	public static final void writeSerializedLocationToDisk(String fileName,Location location,Context context) {
		String serializedLocation = serializeLocation(location, context);
		writeToFile(fileName,serializedLocation, context);
	}

	public static final String serializeLocation(Location location,Context context) {
		StringBuffer sb = new StringBuffer();
		
		long time = System.currentTimeMillis();
		
		sb.append(time);
		sb.append("|");
		sb.append(Utils.parseDate(time,context));
		sb.append("|");
		sb.append(location.getLatitude());
		sb.append("|");
		sb.append(location.getLongitude());
		sb.append("|");
		sb.append(location.getAccuracy());
		sb.append("|");
		sb.append(location.getBearing());
		sb.append("|");
		//sb.append(String.valueOf(location.getElapsedRealtimeNanos()));
		//sb.append("|");
		sb.append(location.getSpeed());
		sb.append("|");
		sb.append(location.getTime());
		sb.append("|");
		sb.append(location.getProvider());
		return sb.toString();
	}

	public static String parseDate(Context context) {
		return parseDate(System.currentTimeMillis(),context);
	}
}
