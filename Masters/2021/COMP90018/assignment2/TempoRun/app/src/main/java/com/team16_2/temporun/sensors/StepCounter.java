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

import javax.xml.transform.dom.DOMLocator;

public class StepCounter implements SensorEventListener {
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor sensor;
    int lastCount;
    Integer startingValue;

    public StepCounter(Context context){
        mContext = context;
        enableSensor();
    }

    public void enableSensor() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor= mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (mSensorManager == null) {
            Log.v("sensor..", "Step counter not supported");
            return;
        }

        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

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
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentCount = (int)event.values[0];

            //StepCounter only reset when device reset. This is a work around.
            if (startingValue == null){
                startingValue = currentCount;
            }
            //update internal counter

            lastCount = currentCount - startingValue;

            //show to screen
            TextView stepCount = ((Activity) mContext).findViewById(R.id.stepView);
            stepCount.setText((String.valueOf(lastCount)));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getTotal(){
        return String.valueOf(lastCount);
    }

}

