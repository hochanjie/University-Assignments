package com.team16_2.temporun.sensors;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.team16_2.temporun.R;

public class Accelerometer implements SensorEventListener {
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor sensor;

    public Accelerometer(Context context) {
        mContext = context;
        enableSensor();
    }

    public void enableSensor() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (mSensorManager == null) {
            Log.v("senor..", "Sensors not supported");
        }

        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void disableSensor() {
        if(mSensorManager != null){
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null){
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            TextView pressure = ((Activity) mContext).findViewById(R.id.speedView);
            pressure.setText((String.valueOf(event.values[0])));
            //EventBus.getDefault().post(new BarometerMessage(event.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
