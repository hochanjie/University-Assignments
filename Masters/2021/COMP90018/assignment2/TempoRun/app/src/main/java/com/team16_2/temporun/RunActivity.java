package com.team16_2.temporun;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.team16_2.temporun.sensors.Distance;
import com.team16_2.temporun.sensors.StepCounter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RunActivity extends AppCompatActivity {

    public static final int SKIP_SONG_THRESHOLD = 3;
    public static final int MAX_SONG_NAME_LENGTH = 12;
    private FirebaseAuth auth;
    TextView mdistance;
    DonutProgressView donut;
    TextView steps;
    TextView speed;
    TextView mainDisplay;
    Distance distance;
    StepCounter stepCounter;
    
    ImageView play;
    ImageView pause;
    ImageView backMusic;
    ImageView fwdMusic;
    String playlist;
    TextView songName;
    TextView artistName;
    ImageView albumPicture;
    SpotifyAppRemote mSpotifyAppRemote = MainActivity.mSpotifyAppRemote;
    PlayerApi playerApi = MainActivity.playerApi;

    private String startTime;
    private String endTime;
    private String totalSteps;
    private String totalDistance;

    boolean paused = false;
    //Global variable to store timer's remaining time
    //This will be updated per second by timer service
    private long timeInSeconds;
    long timerPauseValue;
    long goalTime;
    //Instantiate listener
    private BroadcastReceiver _refreshReceiver = new MyReceiver();

    //Handle intent
    IntentFilter filter;
    Intent intent;
    int minSpeed;
    double goalDistance;
    String donutColor;
    TextView speedometer;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        auth = FirebaseAuth.getInstance();


        intent = getIntent();
        minSpeed = intent.getExtras().getInt("minSpeed");
        TextView title = findViewById(R.id.titleOption);


        //Set main marker as timer or distance
        if(intent.hasExtra("goalDistance")){
            donutColor = "#E300E3";
            goalDistance = intent.getExtras().getInt("goalDistance");
            title.setText("Distance");
        } else {
            donutColor = "#10E7E7";
            title.setText("Timer");
        }
        donut = findViewById(R.id.donutView);
        ArrayList<DonutSection> dataDonut = new ArrayList<DonutSection>();
        donut.setCap(100f);
        dataDonut.add(new DonutSection("section_1", Color.parseColor(donutColor),25f));
        donut.submitData(dataDonut);

        // Sensor control

        ImageView back = findViewById(R.id.imageViewBackBtn);
        ImageView buttonPause = findViewById(R.id.pauseButton);
        ImageView buttonStop = findViewById(R.id.stopBtn);

        //Spotify controls

        play = findViewById(R.id.playBtn);
        pause = findViewById(R.id.pauseBtn);

        backMusic = findViewById(R.id.backMusicBtn);
        fwdMusic = findViewById(R.id.fwdMusicBtn);
        songName = findViewById(R.id.songName3);
        artistName = findViewById(R.id.artistName);
        albumPicture = findViewById(R.id.albumPicture);

        playerApi.subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    Log.d("RunActivity", "got playerState"+playerState.toString());
                    updatePlayPauseButton(playerState);
                    final Track track = playerState.track;
                    if (track != null) {
                        //Limit name length to display
                        String name = track.name;
                        name = name.substring(0, Math.min(name.length(), MAX_SONG_NAME_LENGTH));
                        songName.setText(name);
                        artistName.setText(track.artist.name);
                        mSpotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.SMALL)
                                .setResultCallback(
                                        bitmap -> {
                                            albumPicture.setImageBitmap(bitmap);
                                        });
                        Log.d("RunActivity", track.name + " by " + track.artist.name);
                    }
                });

        startTime = LocalDateTime.now().toString();
        speed = findViewById((R.id.speedView));

        distance = new Distance(this, goalDistance);

        steps = findViewById(R.id.stepView);
        stepCounter = new StepCounter(this);

        //Bind listener to timer service
        filter = new IntentFilter("COUNTDOWN_UPDATED");
        this.registerReceiver(_refreshReceiver, filter);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RunActivity.this, MainActivity.class));
                finish();
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("STOP RUN", "RUN STOP CLICKED");
                endingRun();

            }
        });
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Pause RUN", "RUN PAUSE CLICKED");

                if (!paused){
                    buttonPause.setImageResource(R.drawable.playblue);
                    paused = true;
                    //timer
                    timerPause();
                    //steps:
                    stepCounter.disableSensor();

                    //distance
                    distance.stopLocalisation();
                    distance.resetLocationsList();

                } else {
                    buttonPause.setImageResource(R.drawable.bluepause2);
                    paused = false;
                    timerResume();
                    stepCounter.enableSensor();
                    distance.startLocalisation();
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().resume();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().pause();
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
            }
        });
        backMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
            }
        });
        fwdMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
            }
        });
        // end run if goal distance is reached

        if (goalDistance > 0){
            mainDisplay = findViewById(R.id.mainDisplay);
            mainDisplay.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    double currentDistance = distance.distance;
                    if(currentDistance >= goalDistance){
                        endingRun();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }

        // Main logic

        speed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               double currentSpeed = Double.parseDouble(speed.getText().toString());
               // If current Speed is below min speed pause music
               if(currentSpeed < minSpeed){
                   mSpotifyAppRemote.getPlayerApi().pause();
                   play.setVisibility(View.VISIBLE);
                   pause.setVisibility(View.GONE);
               } else {
                   mSpotifyAppRemote.getPlayerApi().resume();
                   play.setVisibility(View.GONE);
                   pause.setVisibility(View.VISIBLE);
               }
               // If user wants to skip a song he needs to run faster than the minSpeed times a threshold value (3)
               if(currentSpeed > minSpeed * SKIP_SONG_THRESHOLD){
                   mSpotifyAppRemote.getPlayerApi().skipNext();
               }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void endingRun() {
        stopService(new Intent(RunActivity.this, CountDownTimerService.class));
        totalSteps = stepCounter.getTotal();
        totalDistance = distance.getTotal();
        runToDataBase();
        Log.w("STOP RUN", "Data loaded");
        finish();
    }


    @Override
    protected void onDestroy(){
        //accelerometer.disableSensor();
        distance.stopLocalisation();
        stepCounter.disableSensor();
        stopService(new Intent(RunActivity.this, CountDownTimerService.class));
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void runToDataBase(){

        Map<String, Object> runHistory = new HashMap<String, Object>();
        runHistory.put("date", LocalDateTime.now().toString()); // yyyy-MM-dd'T'HH:mm:ss.SSS
        runHistory.put("distance", totalDistance);
        runHistory.put("speed",distance.getSpeed());
        runHistory.put("steps", totalSteps == null ? "0": totalSteps);
        runHistory.put("startTime", startTime);
        runHistory.put("endTime", LocalDateTime.now().toString());
        FirebaseDatabase.getInstance().getReference().child("users").
                child(auth.getUid()).child("run_history").push().updateChildren(runHistory)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Firebase load","Run History saved");
                        }else{

                        }
                    }
                });


    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        //This will be invoked per seconds if timer is still ticking
        public void onReceive(Context context, Intent intent) {
            //This is the time remaining in seconds
            if(timeInSeconds == 0){
                goalTime = intent.getExtras().getLong("time");
            }
            timeInSeconds = intent.getExtras().getLong("time");
            String time = String.format("%02d:%02d:%02d", timeInSeconds/3600,(timeInSeconds % 3600)/60, (timeInSeconds % 60));
            mainDisplay = findViewById(R.id.mainDisplay);
            mainDisplay.setText(time);

            //Update donat
            float progress = (float) ((goalTime - timeInSeconds)/(float)goalTime);
            ArrayList<DonutSection> dataDonut = new ArrayList<DonutSection>();
            dataDonut.add(new DonutSection("section_1", Color.parseColor(donutColor), progress*100));
            donut.submitData(dataDonut);
            if ( progress >= 1){
                endingRun();
            }

        }
    }
    private void updatePlayPauseButton(PlayerState playerState) {
        if (playerState.isPaused) {
            pause.setVisibility(View.GONE);
            play.setVisibility(View.VISIBLE);
        }
        else {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
        }
    }
    public void timerPause(){
        timerPauseValue = timeInSeconds;
        stopService(new Intent(RunActivity.this, CountDownTimerService.class));
    }
    public void timerResume(){
        startService(new Intent(RunActivity.this, CountDownTimerService.class).putExtra("seconds",timerPauseValue));
        filter = new IntentFilter("COUNTDOWN_UPDATED");
        _refreshReceiver = new MyReceiver();
        this.registerReceiver(_refreshReceiver, filter);
    }

}