package com.team16_2.temporun;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class SettingsMenu extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] genders = { "Male", "Female"};
    private FirebaseAuth auth;
    private Button logout;
    private Button update;
    private Spinner gender;
    private EditText age;
    private ImageView avatar;
    private ImageView back;
    private String myGender;
    private EditText userName;
    private String PreGender;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_settings_menu);
        logout = findViewById(R.id.logout);
        back = findViewById(R.id.back);
        update = findViewById(R.id.update);
        userName = findViewById(R.id.userName);
        gender = findViewById(R.id.gender);
        gender.setOnItemSelectedListener(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        age = findViewById(R.id.age);
        avatar = findViewById(R.id.avatar);
        avatar.setBackgroundColor(Color.rgb(255, 255, 255));
        avatar.setOnClickListener(v -> choosePicture());
        StorageReference gsReference = storageReference.child("images").child(auth.getUid());
        gsReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    imageUri = task.getResult();
                    Glide.with(SettingsMenu.this)
                            .load(imageUri.toString())
                            .into(avatar);
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    userName.setHint(String.valueOf(task.getResult().child("username").getValue()));
                    age.setHint(String.valueOf(task.getResult().child("age").getValue()));
                    PreGender = String.valueOf(task.getResult().child("gender").getValue());
                    if(PreGender.equals("Male")){
                        gender.setSelection(0);
                    } else{
                        gender.setSelection(1);
                    }
                }
            }
        });
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item, genders);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(aa);
        update.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("gender").setValue(myGender);
            if(!TextUtils.isEmpty(age.getText())){
                FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("age").setValue(age.getText().toString());
            }
            if(!TextUtils.isEmpty(userName.getText())){
                FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid()).child("username").setValue(userName.getText().toString());
            }
        });
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(SettingsMenu.this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsMenu.this, StartActivity.class));
            finish();
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsMenu.this, MainActivity.class));
                finish();
            }
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imageUri = data.getData();
                        avatar.setImageURI(imageUri);
                        uploadPic();
                    }
                }
            });

    private void uploadPic() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();

        StorageReference riversRef = storageReference.child("images/" + auth.getUid());
        UploadTask uploadTask = riversRef.putFile(imageUri);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(),"Failed to upload", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Snackbar.make(findViewById(android.R.id.content),"Image uploaded",Snackbar.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage: " + (int) progressPercent + "%");
            }
        });
    }


    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        ((TextView) arg0.getChildAt(0)).setTextColor(Color.WHITE);
        myGender = genders[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}