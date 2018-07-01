package com.example.android.flydays;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class UserLocation {
    //todo: location enabled
    // https://codota.com/code/java/methods/android.location.LocationManager/requestLocationUpdates
    // better taken from http://stackoverflow.com/a/3145655#1#L0


    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean permGranted = false;
    boolean network_enabled=false;

    private static final String LOG_TAG = Location.class.getName();

    public boolean getLocation(Context context, LocationResult result){
                //I use LocationResult callback class to pass location value from Location to user code.
        locationResult = result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            permGranted = ContextCompat.checkSelfPermission
                    ( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Problem with location permission", ex);
        }

        //don't start listeners if no provider is enabled
        //code for checking permission:https://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager
        if (!network_enabled && !permGranted)
        {
            return false;
        }else {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                    locationListenerNetwork);
            timer1 = new Timer();
            timer1.schedule(new GetLastLocation(), 20000);
            return true;
        }
    }


    final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(android.location.Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
            lm.removeUpdates(this);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class GetLastLocation extends TimerTask {

        @Override
        public void run() {
            lm.removeUpdates(locationListenerNetwork);
            Location net_loc = null;


            if (network_enabled && permGranted && lm != null)
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (net_loc != null) {
                locationResult.gotLocation(net_loc);
                return;
            }
            locationResult.gotLocation(null);
        }
    }


    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
