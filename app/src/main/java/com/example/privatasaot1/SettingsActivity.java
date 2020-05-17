package com.example.privatasaot1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private String getType;

    private CircleImageView profileImageView;
    private EditText nameEditText, phoneEditText, driverCarName;
    private ImageView closeButton, saveButton;
    private TextView profileChangeBtn;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private String checker = "";

    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsRefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        getType = getIntent().getStringExtra("type");

        Toast.makeText(this, getType, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(getType);
        storageProfilePicsRefer = FirebaseStorage.getInstance().getReference()
                .child("Profile Pictures");

        profileImageView = findViewById(R.id.profile_image);

        nameEditText = findViewById(R.id.name);
        phoneEditText = findViewById(R.id.phone_number);
        driverCarName = findViewById(R.id.driver_car_name);

        if (getType.equals("Drivers")) {
            driverCarName.setVisibility(View.VISIBLE);
        }


        closeButton = findViewById(R.id.close_button);
        saveButton = findViewById(R.id.save_button);
        profileChangeBtn = findViewById(R.id.change_picture_btn);


        closeButton.setOnClickListener(view -> {
            if (getType.equals("Drivers")) {

                startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));

            } else {

                startActivity(new Intent(SettingsActivity.this, CustomersMapsActivity.class));
            }
        });

        saveButton.setOnClickListener(view -> {
            if (checker.equals("clicked")) {

                validateControllers();


            } else {


                validateAndSaveOnlyInformation();

            }
        });

        profileChangeBtn.setOnClickListener(view -> {
            checker = "clicked";
            CropImage.activity().setAspectRatio(1, 1)
                    .start(SettingsActivity.this);
        });
getUserInformation();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUri = result.getUri();
            profileImageView.setImageURI(imageUri);
        } else {
            if (getType.equals("Drivers")) {
                startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));
            } else {


                startActivity(new Intent(SettingsActivity.this, CustomersMapsActivity.class));

            }

            Toast.makeText(this, "Error, Try Again", Toast.LENGTH_SHORT).show();

        }


    }

    private void validateControllers() {

        if (TextUtils.isEmpty(nameEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your name", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your phone number", Toast.LENGTH_SHORT).show();

        } else if (getType.equals("Drivers") && TextUtils.isEmpty(driverCarName.getText().toString())) {
            Toast.makeText(this, "Please provide your car Name", Toast.LENGTH_SHORT).show();

        } else if (checker.equals("clicked")) {
            uploadProfilePicture();
        }
    }

    private void uploadProfilePicture() {


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Settings Account Information");
        progressDialog.setMessage("Please wait, while we are settings your account information");
        progressDialog.show();


        if (imageUri != null) {

            final StorageReference fileRef = storageProfilePicsRefer.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {

                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    if (downloadUrl != null) { //bdika shel if
                        myUrl = downloadUrl.toString();//   myUrl = downloadUrl.toString();
                    }

                    HashMap<String, Object> uderMap = new HashMap<>();
                    uderMap.put("uid", mAuth.getCurrentUser().getUid());
                    uderMap.put("name", nameEditText.getText().toString());
                    uderMap.put("phone", phoneEditText.getText().toString());
                    uderMap.put("image", myUrl);

                    if (getType.equals("Drivers")) {
                        uderMap.put("car", driverCarName.getText().toString());
                    }

                    databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(uderMap);

                    progressDialog.dismiss();

                    if (getType.equals("Drivers")) {

                        startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));

                    } else {
                        startActivity(new Intent(SettingsActivity.this, CustomersMapsActivity.class));

                    }
                }
            });
        } else {
            Toast.makeText(this, "Image is not selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndSaveOnlyInformation() {

        if (TextUtils.isEmpty(nameEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your name", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please provide your phone number", Toast.LENGTH_SHORT).show();

        } else if (getType.equals("Drivers") && TextUtils.isEmpty(driverCarName.getText().toString())) {
            Toast.makeText(this, "Please provide your car Name", Toast.LENGTH_SHORT).show();

        } else {
            HashMap<String, Object> uderMap = new HashMap<>();
            uderMap.put("uid", mAuth.getCurrentUser().getUid());
            uderMap.put("name", nameEditText.getText().toString());
            uderMap.put("phone", phoneEditText.getText().toString() );


            if (getType.equals("Drivers")) {
                uderMap.put("car", driverCarName.getText().toString());
            }

            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(uderMap);


            if (getType.equals("Drivers")) {

                startActivity(new Intent(SettingsActivity.this, DriversMapsActivity.class));

            } else {
                startActivity(new Intent(SettingsActivity.this, CustomersMapsActivity.class));

            }
        }

    }


    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    nameEditText.setText(name);
                    phoneEditText.setText(phone);

                    if (getType.equals("Drivers")) {

                        String car = dataSnapshot.child("car").getValue().toString();
                        driverCarName.setText(car);
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }
}
