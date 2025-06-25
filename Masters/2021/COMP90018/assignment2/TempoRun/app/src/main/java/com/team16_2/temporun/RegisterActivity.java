package com.team16_2.temporun;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;


import android.os.Bundle;

import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirm;
    private Button register;
    private ImageView back;
    private FirebaseAuth auth;
    private final Integer passwordLength = 8;
    private static final Pattern emailPattern = Pattern.compile("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$");

    private Button connectSpotify;

    // replace with your own Spotify app's client_id if you want to import your own playlist
    private static final String CLIENT_ID =  "4681123014aa4732989ee0f1d094582e";
    private static final String REDIRECT_URI = "http://com.temporun/callback";
    public static final int REQUEST_CODE = 1337;

    public static OkHttpClient mOkHttpClient = new OkHttpClient();
    public static String mAccessToken;
    public static Call mCall;
    public static String playlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.confirm);
        register = findViewById(R.id.register);
        back = findViewById(R.id.imageViewBackBtn);

        connectSpotify = findViewById(R.id.connectSpotify);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(RegisterActivity.this, StartActivity.class));
               finish();
            }
        });

        auth = FirebaseAuth.getInstance();

        register.setOnClickListener(v -> {
            String textUsername = username.getText().toString();
            String textEmail = email.getText().toString();
            String textPass = password.getText().toString();
            String textConfirm = confirm.getText().toString();

            if (TextUtils.isEmpty(textEmail) || TextUtils.isEmpty(textPass)) {
                Toast.makeText(RegisterActivity.this, "Empty email or password", Toast.LENGTH_SHORT).show();
            } else if (!emailPattern.matcher(textEmail).matches()) {
                Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            } else if (textPass.length() < passwordLength) {
                Toast.makeText(RegisterActivity.this, "Password should contain more than 8 chars", Toast.LENGTH_SHORT).show();
            } else if (!textPass.equals(textConfirm)) {
                Toast.makeText(RegisterActivity.this, "Password mismatch", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(textEmail, textPass, textUsername);
            }
        });

        connectSpotify.setOnClickListener(v -> {
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
            builder.setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming", "user-read-recently-played"});
            builder.build();
            AuthorizationRequest request = builder.build();
            AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
        });

    }
    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void registerUser(String email, String password, String username) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Registration succeed", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("username").setValue(username);
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Toast.makeText(RegisterActivity.this, "Authentication succeeded", Toast.LENGTH_SHORT).show();
                    // Handle successful response
                    String token = response.getAccessToken();
                    // Log.d("token", token);
                    mAccessToken = token;
                    final Request request = new Request.Builder()
                            //.url("https://api.spotify.com/v1/me")
                            .url("https://api.spotify.com/v1/me/playlists")
                            .addHeader("Authorization","Bearer " + mAccessToken)
                            .build();

                    cancelCall();
                    mCall = mOkHttpClient.newCall(request);
                    mCall.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("failure", "failure");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                // Log.d("response", response.body().string());
                                JSONObject jsonObject = new JSONObject(response.body().string());

                                JSONArray items = jsonObject.getJSONArray("items");
                                List<JSONObject> playlists = new ArrayList<>();
                                for(int i = 0; i < items.length(); i++){
                                    try {
                                        playlists.add((JSONObject) items.get(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                playlist = playlists.get(0).getString("uri");
                                Log.d("playlist", playlist);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.e("auth error", response.getError());
                    Toast.makeText(RegisterActivity.this, "Authentication failed. Using default playlist", Toast.LENGTH_SHORT).show();
                    playlist = "spotify:playlist:37i9dQZF1DXcBWIGoYBM5M?si=41ee568d32fe47c2";
                    break;

                // Most likely auth flow was cancelled
                default:
                    Toast.makeText(RegisterActivity.this, "Authentication cancelled. Try again", Toast.LENGTH_SHORT).show();
                    // Handle other cases
            }
        }
    }

}