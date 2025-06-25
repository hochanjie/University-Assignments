package com.team16_2.temporun;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.spotify.android.appremote.api.AppRemote;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.Track;
import com.team16_2.temporun.fragments.MonthRunFragment;
import com.team16_2.temporun.fragments.NoRunHistoryFragment;
import com.team16_2.temporun.fragments.TodayRunFragment;
import com.team16_2.temporun.fragments.WeekRunFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.*;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    public static final int MAX_SONG_NAME_LENGTH = 12;
    public static SpotifyAppRemote mSpotifyAppRemote;
    private Uri imageUri;
    private ImageView menu;
    private ImageView next;
    private Button today;
    private Button week;
    private Button month;
    private FirebaseAuth auth;
    private TextView username;
    private ConstraintLayout mainLayout;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView avatar;
    private final static Integer numDayWeek = 7 - 1;
    private final static Integer numDayMonth = 30 - 1;

    private Boolean hasRunToday = false, hasRunWeek = false, hasRunMonth = false;
    private ArrayList<RunDetail> todayRun = new ArrayList<>(), weekRun = new ArrayList<>(), monthRun = new ArrayList<RunDetail>();
    private LocalDateTime lt = LocalDateTime.now();

    // replace with your own Spotify app's client_id if you want to import your own playlist
    private static final String CLIENT_ID =  "4681123014aa4732989ee0f1d094582e";
    private static final String REDIRECT_URI = "http://com.temporun/callback";

    ImageView play;
    ImageView pause;
    ImageView backMusic;
    ImageView fwdMusic;
    String playlist;
    TextView songName;
    TextView artistName;
    ImageView albumPicture;

    public static PlayerApi playerApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        menu = findViewById(R.id.menu);
        next = findViewById(R.id.nextButton);
        today = findViewById(R.id.todayBtn);
        week = findViewById(R.id.weekBtn);
        month = findViewById(R.id.monthBtn);
        auth = FirebaseAuth.getInstance();
        avatar = findViewById(R.id.avatar2);
        username = findViewById(R.id.username);

        play = findViewById(R.id.playBtn);
        pause = findViewById(R.id.pauseBtn);

        backMusic = findViewById(R.id.backMusicBtn);
        fwdMusic = findViewById(R.id.fwdMusicBtn);

        playlist = RegisterActivity.playlist;

        songName = findViewById(R.id.songName3);
        artistName = findViewById(R.id.artistName);
        albumPicture = findViewById(R.id.albumPicture);


        if (playerApi != null) {
            playerApi.subscribeToPlayerState()
                    .setEventCallback(playerState -> {
                        Log.d("MainActivity", "got playerState"+playerState.toString());
                        final Track track = playerState.track;
                        if (playerState.isPaused) {
                            pause.setVisibility(View.GONE);
                        }
                        else {
                            play.setVisibility(View.GONE);
                        }
                        if (track != null) {
                            //Limit name length to display
                            String name = track.name;
                            name = name.substring(0, Math.min(name.length(), 12));
                            songName.setText(name);
                            artistName.setText(track.artist.name);
                            mSpotifyAppRemote
                                    .getImagesApi()
                                    .getImage(playerState.track.imageUri, Image.Dimension.SMALL)
                                    .setResultCallback(
                                            bitmap -> {
                                                albumPicture.setImageBitmap(bitmap);
                                            });
                            Log.d("MainActivity", track.name + " by " + track.artist.name);
                        }
                    });
        }
        else {
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();

            SpotifyAppRemote.connect(this, connectionParams,
                    new Connector.ConnectionListener() {
                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            // Toast.makeText(MainActivity.this, "Spotify player connected", Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", "Connected! Yay!");
                            playerApi = mSpotifyAppRemote.getPlayerApi();
                            playerApi.play(playlist);
                            playerApi.pause();
                            pause.setVisibility(View.GONE);
                            playerApi.subscribeToPlayerState()
                                    .setEventCallback(playerState -> {
                                        Log.d("MainActivity", "got playerState" + playerState.toString());
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
                                            Log.d("MainActivity", track.name + " by " + track.artist.name);
                                        }
                                    });

                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e("MyActivity", throwable.getMessage(), throwable);
                            Toast.makeText(MainActivity.this, "Spotify player could not be connected", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        String id = auth.getUid();
        mainLayout = findViewById(R.id.mainlayoutview);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference gsReference1 = storageReference.child("images").child("default.jpg");
        StorageReference gsReference = storageReference.child("images").child(id);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.circleCropTransform();
        requestOptions.transforms( new RoundedCorners(180));

        gsReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUri = task.getResult();
                    Glide.with(MainActivity.this)
                            .applyDefaultRequestOptions(requestOptions)
                            .load(imageUri.toString())
                            .into(avatar);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                gsReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            imageUri = task.getResult();
                            Glide.with(MainActivity.this)
                                    .applyDefaultRequestOptions(requestOptions)
                                    .load(imageUri.toString())
                                    .into(avatar);
                        }
                    }
                });
            }
        });
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                username.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors here
            }
        });

        DatabaseReference runHistoryRef = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("run_history");
        runHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<RunDetail> runData = new ArrayList<RunDetail>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    RunDetail runDetail = snapshot.getValue(RunDetail.class);
                    runData.add(runDetail);
                }

                // Find all running in today/week/month
                for (int i = 0; i < runData.size(); i++)
                {
                    RunDetail current = runData.get(i);

                    if (sameDate(current.getDate(), lt)) {
                        hasRunToday = true;
                        todayRun.add(current);
                    }
                    if (beforeDate(current.getDate(), lt.minusDays(numDayWeek)) >= 0) {
                        hasRunWeek = true;
                        weekRun.add(current);
                    }
                    if (beforeDate(current.getDate(), lt.minusDays(numDayMonth)) >= 0) {
                        hasRunMonth = true;
                        monthRun.add(current);
                    }
                }

                Log.d("has run today", String.valueOf(hasRunToday));
                Log.d("has run week ", String.valueOf(hasRunWeek));
                Log.d("has run month", String.valueOf(hasRunMonth));


                if (hasRunToday) {
                    replaceFragment(new TodayRunFragment());
                } else {
                    replaceFragment(new NoRunHistoryFragment());
                }

            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors here
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsMenu.class));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingRunActivity.class));
            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today.setBackgroundColor(Color.parseColor("#EE00EE"));
                today.setTextColor(Color.parseColor("#FFFFFF"));
                week.setBackgroundColor(Color.parseColor("#FFFFFF"));
                week.setTextColor(Color.parseColor("#952626"));
                month.setBackgroundColor(Color.parseColor("#FFFFFF"));
                month.setTextColor(Color.parseColor("#952626"));

                if (hasRunToday) {
                    replaceFragment(new TodayRunFragment());
                } else {
                    replaceFragment(new NoRunHistoryFragment());
                }

            }
        });

        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today.setBackgroundColor(Color.parseColor("#FFFFFF"));
                today.setTextColor(Color.parseColor("#952626"));
                week.setBackgroundColor(Color.parseColor("#EE00EE"));
                week.setTextColor(Color.parseColor("#FFFFFF"));
                month.setBackgroundColor(Color.parseColor("#FFFFFF"));
                month.setTextColor(Color.parseColor("#952626"));

                if (hasRunWeek) {
                    replaceFragment(new WeekRunFragment());
                } else {
                    replaceFragment(new NoRunHistoryFragment());
                }

            }
        });

        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                today.setBackgroundColor(Color.parseColor("#FFFFFF"));
                today.setTextColor(Color.parseColor("#952626"));
                week.setBackgroundColor(Color.parseColor("#FFFFFF"));
                week.setTextColor(Color.parseColor("#952626"));
                month.setBackgroundColor(Color.parseColor("#EE00EE"));
                month.setTextColor(Color.parseColor("#FFFFFF"));

                if (hasRunMonth) {
                    replaceFragment(new MonthRunFragment());
                } else {
                    replaceFragment(new NoRunHistoryFragment());
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

    }

    private void replaceFragment(Fragment fragment) {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("todayRun", todayRun);
        bundle.putParcelableArrayList("weekRun", weekRun);
        bundle.putParcelableArrayList("monthRun", monthRun);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();

    }

    private Boolean sameDate(LocalDateTime dt1, LocalDateTime dt2) {
        return dt1.getYear() == dt2.getYear() && dt1.getDayOfMonth() == dt2.getDayOfMonth();
    }

    private Integer beforeDate(LocalDateTime dt1, LocalDateTime dt2) {
        return dt1.toLocalDate().compareTo(dt2.toLocalDate());
    }

}
