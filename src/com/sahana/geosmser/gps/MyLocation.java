package com.sahana.geosmser.gps;

import java.util.Observable;
import java.util.Observer;

import com.sahana.geosmser.R;
import com.sahana.geosmser.WhereToMeet;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.UrlQuerySanitizer.ValueSanitizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MyLocation extends Observable{
    public static final int GPS_UPDATE_TIME_INTERVAL = 0;
    public static final int GPS_UPDATE_MINIMUM_DISTANCE = 10;
    
    private Context baseContext;
    private LocationManager mLocationManager;
    private volatile boolean enableGPS = false;
    private Location curLocation;
    
    public int timeInterval = 0;
    public float minDistance = 1.5f;
    public ProvideType mProviderType;
    public MyLocationListener mLocationListener;
    public Handler mHandler;
    
    public enum ProvideType {
        E_GPS, E_AGPS;
        public String getValue() {
            switch(this) {
                case E_GPS:
                    return LocationManager.GPS_PROVIDER;
                case E_AGPS:
                    return LocationManager.NETWORK_PROVIDER;
                default:
                    return LocationManager.GPS_PROVIDER;
            }
        }
    }
    
    public MyLocation(Context context) {
        baseContext = context;
        init();
    }
    
    public MyLocation(Context context, Observer observer) {
        baseContext = context;
        init();
        addObserver(observer);
    }
    
    
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    
    public void init() {
        registerLocationManager();
    }
    
    public void registerLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) baseContext.getSystemService(Context.LOCATION_SERVICE);
        }
    }
    
    public void unregisterLocationManager() {
        mLocationManager = null;
    }
    
    public void notify(Location location) {
        setChanged();
        notifyObservers(location);
    }
    
    public boolean isGPSEnabled() {
        if (enableGPS) {
            if (mLocationManager == null) {
                enableGPS = false;
            }
        }
        return enableGPS;
    }
    
    public boolean enable(ProvideType type) {
        
        boolean gpsEnable = false;
        boolean agpsEnable = false;
        
        gpsEnable = mLocationManager.isProviderEnabled(ProvideType.E_GPS.getValue());
        agpsEnable = mLocationManager.isProviderEnabled(ProvideType.E_AGPS.getValue());
        
        if(gpsEnable || agpsEnable) {
            if (!isGPSEnabled() && !enableGPS) {
                mProviderType = type;
                // curLocation = new Location(type.getValue());
                curLocation = mLocationManager.getLastKnownLocation(type.getValue());
                mLocationListener = new MyLocationListener();
                switch (type) {
                    case E_GPS:
                        break;
                    case E_AGPS:
                        timeInterval = 2500;
                        break;
                }
                mLocationManager.requestLocationUpdates(type.getValue(), timeInterval, minDistance, mLocationListener);
                enableGPS = true;
            }
        } else {
            Toast.makeText(baseContext, R.string.gps_state_no_provider, Toast.LENGTH_SHORT).show();
            enableGPS = false;
        }
        
        return enableGPS;
    }
    
    public void disable() {
        if (isGPSEnabled()) {
            if (mLocationListener != null) {
                mLocationManager.removeUpdates(mLocationListener);
            }
            enableGPS = false;
        }
    }
    
    public void setCurrentLocation(Location location) {
        curLocation = location;
    }
    
    public Location getCurrentLocation() {
        return curLocation;
    }
    
    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            MyLocation.this.notify(location);
        }
        @Override
        public void onProviderDisabled(String provider) {
            
        }
        @Override
        public void onProviderEnabled(String provider) {
            
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    
   
}
