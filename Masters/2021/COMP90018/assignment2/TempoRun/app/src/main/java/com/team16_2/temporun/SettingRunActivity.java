package com.team16_2.temporun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.AppRemote;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.team16_2.temporun.fragments.SetDistanceFragment;
import com.team16_2.temporun.fragments.SetTimerFragment;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SettingRunActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static final int MAX_SONG_NAME_LENGTH = 12;
    private int minSpeed = 2;
    private TextView speedValue;
    private Spinner limitOptions;
    private String optionSelected;
    private String[] options = { "Time", "Distance"};

    private Fragment distanceFragment;
    private Fragment timerFragment;
    String hours;
    String minutes;
    String goalDistance;
    private ItemViewModel viewModel;

    ImageView play;
    ImageView pause;
    ImageView backMusic;
    ImageView fwdMusic;
    TextView songName;
    TextView artistName;
    ImageView albumPicture;

    SpotifyAppRemote mSpotifyAppRemote = MainActivity.mSpotifyAppRemote;
    PlayerApi playerApi = MainActivity.playerApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_run);

        ImageView back = findViewById(R.id.imageViewBackBtn);
        ImageView btnRun = findViewById(R.id.startBtn);
        ImageView btnIncrease = findViewById(R.id.increase);
        ImageView btnDecrease = findViewById(R.id.decrease);
        speedValue = findViewById(R.id.speedValue);
        speedValue.setText(String.valueOf(minSpeed));


        limitOptions = findViewById(R.id.spinner);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item, options);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        limitOptions.setAdapter(aa);
        limitOptions.setOnItemSelectedListener(this);

        play = findViewById(R.id.playBtn2);
        pause = findViewById(R.id.pauseBtn2);
        backMusic = findViewById(R.id.backMusicBtn2);
        fwdMusic = findViewById(R.id.fwdMusicBtn2);
        songName = findViewById(R.id.songName2);
        artistName = findViewById(R.id.artistName2);
        albumPicture = findViewById(R.id.albumPicture2);

        if(playerApi != null){
            playerApi.subscribeToPlayerState()
                    .setEventCallback(playerState -> {
                        Log.d("SettingRunActivity", "got playerState"+playerState.toString());
                        final Track track = playerState.track;
                        updatePlayPauseButton(playerState);
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
                            Log.d("SettingRunActivity", track.name + " by " + track.artist.name);
                        }
                    });
        }



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


        //Listen to fragment changes
        getSupportFragmentManager().setFragmentResultListener("requestDistance", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                goalDistance = result.getString("goalDistance");
            }
        });
        getSupportFragmentManager().setFragmentResultListener("requestMinutes", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                minutes = result.getString("minutes");
            }
        });
        getSupportFragmentManager().setFragmentResultListener("requestHours", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                hours = result.getString("hours");
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingRunActivity.this, MainActivity.class));
                finish();
            }
        });

        btnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Create new Run
                if (optionSelected.equals("Time")){
                    //Start timer
                    startTimer(hours, minutes);
                    startActivity(new Intent(SettingRunActivity.this, RunActivity.class).putExtra("minSpeed", minSpeed));
                } else {
                    startActivity(new Intent(SettingRunActivity.this, RunActivity.class).putExtra("minSpeed", minSpeed).putExtra("goalDistance",Integer.parseInt(goalDistance)));
                }
            }
        });
        btnIncrease.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("Increase","Increase button click");
                minSpeed++;
                speedValue.setText(String.valueOf(minSpeed));
            }
        });
        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Decrease", "Decrease button click");
                minSpeed--;
                speedValue.setText(String.valueOf(minSpeed));
            }
        });

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
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);

        optionSelected = options[i];
        if(optionSelected.equals("Distance")){
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).replace(R.id.fragmentContainerView, SetDistanceFragment.class, null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).replace(R.id.fragmentContainerView, SetTimerFragment.class, null).commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void startTimer(String hours, String minutes){
        Log.d("COUNTDOWN", "YESYES CLICKED");
        if(!TextUtils.isEmpty(hours) &&!TextUtils.isEmpty(minutes)){
            startService(new Intent(SettingRunActivity.this, CountDownTimerService.class).putExtra("hours",Integer.parseInt(hours))
                    .putExtra("minutes",Integer.parseInt(minutes)));
        }else if (TextUtils.isEmpty(hours)&&TextUtils.isEmpty(minutes)){
            startService(new Intent(SettingRunActivity.this, CountDownTimerService.class).putExtra("hours",0)
                    .putExtra("minutes",0));
        } else if (TextUtils.isEmpty(hours)){
            startService(new Intent(SettingRunActivity.this, CountDownTimerService.class).putExtra("hours",0)
                    .putExtra("minutes",Integer.parseInt(minutes)));
        }else if (TextUtils.isEmpty(minutes)){
            startService(new Intent(SettingRunActivity.this, CountDownTimerService.class).putExtra("hours",Integer.parseInt(hours))
                    .putExtra("minutes",0));
        }
    }
    public String getMinutes(){
        return minutes;
    }
    public String getHours(){
        return hours;
    }
    public String getGoalDistance(){
        return goalDistance;
    }
}
