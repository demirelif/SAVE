package com.example.saveandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;

public class LocationTracker { //implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    private static final String TAG = "LocationTracker";
    private final Context owner;
    private final Method method;
    //    private GoogleApiClient mGoogleApiClient;
//    private FusedLocationProviderClient fusedLocationClient;
//    private LocationCallback mLocationCallback;
    private LocationManager mLocationManager;
    private long minTime;
    private float minDistance;
    private ILocationTrackerCallBack callBack;
    private LocationListener locListener = null;

    public LocationTracker(Context owner, Method method)  {
        this.owner = owner;
        this.method = method;
        mLocationManager = (LocationManager) owner.getSystemService(Context.LOCATION_SERVICE);
//        checkLocation(); //check whether location service is enable or not in your  phone
//        mGoogleApiClient = null;
    }

    @SuppressLint("MissingPermission")
    public void StartTracking(long minTime, float minDistance, ILocationTrackerCallBack callBack) throws Exception {
        Log.i(TAG, "StartTracking");
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.callBack = callBack;
//        if (method == Method.PlayServices) {
//            if (mGoogleApiClient == null)
//                mGoogleApiClient = new GoogleApiClient.Builder(owner)
//                        .addConnectionCallbacks(this)
//                        .addOnConnectionFailedListener(this)
//                        .addApi(LocationServices.API)
//                        .build();
//
//            mGoogleApiClient.connect();
//        } else //if(method == Method.Location)
        {
            locListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    callBack.onLocationChanged(location);
                }
            };
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locListener);
        }
    }

    public void StopTracking() {
        Log.i(TAG, "StopTracking");
//        if (method == Method.PlayServices) {
//            {
//                if (fusedLocationClient != null) {
//                    fusedLocationClient.removeLocationUpdates(mLocationCallback);
//                    fusedLocationClient = null;
//                }
//                if (mGoogleApiClient != null) {
//                    mGoogleApiClient.disconnect();
//                    mGoogleApiClient = null;
//                }
//            }
//        } else
        {
            if(locListener!=null) {
                mLocationManager.removeUpdates(locListener);
                locListener = null;
            }
        }
    }

//    @SuppressLint("MissingPermission")
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.i(TAG, "onConnected");
//        LocationRequest mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(updateInterval)
//                .setFastestInterval(fastestInterval);
//
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//                callBack.onLocationChanged(locationResult.getLastLocation());
//            }
//        };
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(owner);
//        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
//    }

//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.i(TAG, "onConnectionSuspended");
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
//    }
//
//    private void checkLocation() throws Exception {
//        boolean tut = isLocationEnabled();
//        if (!tut)
//            throw new Exception("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use Application");
//    }

//    private boolean isLocationEnabled() {
//        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//    }

    public enum Method {
        PlayServices,
        Location
    }
}
