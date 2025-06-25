package com.team16_2.temporun.sensors;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.team16_2.temporun.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalDouble;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class Distance {

    public static final double MIN_ACCEPTABLE_ACCURACY = 20.0;
    public static final int SAMPLE_SIZE = 5;
    public static final int MIN_BATTERY_THRESHOLD = 20;
    private Context mContext;
    private LocationManager lm;
    LocationListener locationListener;
    private double[] prevLoc = new double[2];
    public double distance = 0.0;
    private ArrayList<Location> locationsList = new ArrayList<Location>();
    public ArrayList<Double> averageSpeed = new ArrayList<Double>();
    int noiseCounter = 0;
    double goalDistance;

    IntentFilter ifilter;
    Intent batteryStatus;

    public Distance(Context context, double goal){
        mContext = context;
        goalDistance = goal;
        checkGPSSettings();

    }


    public void checkGPSSettings() {
        lm = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = mContext.registerReceiver(null, ifilter);

        //Overrides/implements LocationListener class
        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }

            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                double longitudeO = location.getLongitude();
                double latitudeO = location.getLatitude();

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryPct = level * 100 / (float)scale;

                if(batteryPct < MIN_BATTERY_THRESHOLD){
                    lm.removeUpdates(locationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }

                TextView cdistance = ((Activity) mContext).findViewById(R.id.mainDisplay);
                DonutProgressView donut = ((Activity) mContext).findViewById(R.id.donutView);
                TextView speed = ((Activity) mContext).findViewById((R.id.speedView));

                //

                if(Double.isNaN(prevLoc[0])){
                    Log.d("onLocChanged", "is NAN...");
                    prevLoc[0] = latitudeO;
                    prevLoc[1] = longitudeO;
                }

                // Median filter implementation
                //Calculate median of last measures distances to clean noise

                double sample = location.getAccuracy() <= MIN_ACCEPTABLE_ACCURACY ? location.distanceTo(locationsList.get(locationsList.size() - 1)) : 0.0;
                double[] median = new double[SAMPLE_SIZE];

                if (locationsList.size() >= SAMPLE_SIZE){
                    for(int i = 1; i <= SAMPLE_SIZE; i++){
                        median[i-1] = location.distanceTo(locationsList.get(locationsList.size() - i));
                        //sample += location.distanceTo(locList.get(locList.size() - i));
                    }
                    Arrays.sort(median);
                    sample = median[(int) Math.floor(median.length/2)];
                }


                //Store location

                prevLoc[0] = latitudeO;
                prevLoc[1] = longitudeO;
                locationsList.add(location);

                // Process every SAMPLE_SIZE updates

                noiseCounter++;

                if (noiseCounter%SAMPLE_SIZE == 0){
                    distance += sample;

                    //Prevent to change donut if timer is set
                    if(goalDistance > 0.0f){
                        cdistance.setText(String.format("%.1f",distance));
                        float progress = (float) (distance/goalDistance);
                        ArrayList<DonutSection> dataDonut = new ArrayList<DonutSection>();
                        dataDonut.add(new DonutSection("section_1", Color.parseColor("#E300E3"),progress*100));
                        donut.submitData(dataDonut);
                    }

                    averageSpeed.add((double) location.getSpeed());
                    speed.setText(String.format("%.2f", location.getSpeed()));
                }

                Log.d("on loc listener", "" + location.getProvider() + " Location latitude " + latitudeO + "\nlongitude:" + longitudeO);
            }
        };

        // Location hardware setting enabled?
        boolean GPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        String[] permissionsArray = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };

        if (GPSEnabled) {
            // Android 6.0+
            if (Build.VERSION.SDK_INT >= 23) {
                if (!checkPermissions(mContext, permissionsArray)) {
                    // request code 1
                    Log.d("haha", "request");
                    ActivityCompat.requestPermissions((Activity) mContext, permissionsArray,
                            1);
                } else {
                    // Permission has already been granted
                    Log.d("haha", "line 52");
                    startLocalisation();
                }

            } else {
                // no runtime check
                Log.d("haha", "line 74");
                startLocalisation();
            }
        } else {
            Log.d("haha", "line 82");
            Toast.makeText(mContext, "GPS Not Enabled", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            // request code 2
            //startActivityForResult(intent, 2);
        }
    }

    public void startLocalisation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String providerNET = LocationManager.NETWORK_PROVIDER;
        String providerGPS = LocationManager.GPS_PROVIDER;

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }

        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location net_loc = null, gps_loc = null, finalLoc = null;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float)scale;


        if (gps_enabled && batteryPct >= MIN_BATTERY_THRESHOLD) {
            Log.d("haha", " gps_enabled");

            lm.requestLocationUpdates(providerGPS, 0, 0, locationListener);
            gps_loc = lm.getLastKnownLocation(providerGPS);
        }
        if (network_enabled){
            Log.d("haha", " net_enabled");
            lm.requestLocationUpdates(providerNET, 0, 0, locationListener);
            net_loc = lm.getLastKnownLocation(providerNET);
        }

        if (gps_loc != null && net_loc != null) {

            Log.d("haha", "both available location");

            //smaller the number more accurate result will
            if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;
            // I used this just to get an idea (if both avail, its upto you which you want to take as I've taken location with more accuracy)

        } else {

            if (gps_loc != null) {
                finalLoc = gps_loc;
                Log.d("haha", "gps available location");
            } else if (net_loc != null) {
                finalLoc = net_loc;
                Log.d("haha", "net available location");
            }
        }


        if (finalLoc != null) {
            double latitude = finalLoc.getLatitude();
            double longitude = finalLoc.getLongitude();
            Log.d("startLoc", "latitude：" + latitude + "\nlongitude" + longitude);

            prevLoc[0] = latitude;
            prevLoc[0] = longitude;
            locationsList.add(finalLoc);
            // if we are in melbourne, we get negative latitude.
            // it means south part of the earth.
        } else {
            Log.d("haha", "no available location");
            //startLocalisation();
        }
    }

    public void stopLocalisation(){
        lm.removeUpdates(locationListener);
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission: permissions ) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        // may need to be deleted
        return true;
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permision granted", "granted location");
                    startLocalisation();
                } else {
                    //TODO
                }
                return;
            }
            default:
                break;
        }
    }

    public String getTotal(){
        return String.valueOf((int)distance);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getSpeed(){
        OptionalDouble avg = averageSpeed.stream().mapToDouble(a->a).average();
        return avg.isPresent() ? String.valueOf(avg.getAsDouble()) : "0";
    }

    public void resetLocationsList(){
        locationsList.clear();
    }

    public double getAVGspeed(){ return (double) averageSpeed.get(averageSpeed.size() - 1); }

}
