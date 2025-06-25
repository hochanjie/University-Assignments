package com.team16_2.temporun;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class CountDownTimerService extends Service {
    static long TIME_LIMIT = 0;
    CountDownTimer Count;
    Boolean isRunning=false;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(isRunning==true){
            return START_NOT_STICKY;
        }
        isRunning = true;
        Integer hours=0;
        Integer minutes=0;
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if(intent.hasExtra("seconds")){
                TIME_LIMIT = (int) extras.getLong("seconds") * 1000;
            } else {
                hours = extras.getInt("hours");
                minutes = extras.getInt("minutes");
                TIME_LIMIT = hours * 3600000 + minutes * 60000;
            }
        }
        Count = new CountDownTimer(TIME_LIMIT, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                String time = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                Intent i = new Intent("COUNTDOWN_UPDATED");
                Log.d("COUNTDOWN", time);
                i.putExtra("time", seconds);
                sendBroadcast(i);
                //coundownTimer.setTitle(millisUntilFinished / 1000);

            }

            public void onFinish() {
                //coundownTimer.setTitle("Sedned!");
                Intent i = new Intent("COUNTDOWN_UPDATED");
                i.putExtra("countdown","Sent!");
                isRunning = false;
                sendBroadcast(i);
                //Log.d("COUNTDOWN", "FINISH!");
                stopSelf();

            }
        };

        Count.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Count.cancel();
        super.onDestroy();
    }


}
