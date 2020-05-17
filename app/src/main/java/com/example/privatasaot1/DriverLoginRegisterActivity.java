package com.example.privatasaot1;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.google.firebase.auth.FirebaseAuth.*;

public class DriverLoginRegisterActivity extends AppCompatActivity {
    private TextView CreateDriverAccount;
    private TextView TitleDriver;
    private Button LoginDriverButton;
    private Button RegisterDriverButton;
    private EditText driverEmail;
    private EditText driverPassword;
    private DatabaseReference driversDatabaseRef;
    private FirebaseAuth mAuth;
    private AuthStateListener firebaseAuthListner;

    private ProgressDialog loadingBar;
    private FirebaseUser currentUser;
    String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);

        //
        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListner = firebaseAuth -> {
            currentUser = getInstance().getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapsActivity.class);
                startActivity(intent);
            }
        };


        CreateDriverAccount = findViewById(R.id.register_link_driver);
        TitleDriver = findViewById(R.id.driver_status);
        LoginDriverButton = findViewById(R.id.driver_button_login);
        RegisterDriverButton = findViewById(R.id.driver_button_register);
        driverEmail = findViewById(R.id.email_driver);
        driverPassword = findViewById(R.id.password_driver);
        loadingBar = new ProgressDialog(this);


        RegisterDriverButton.setVisibility(View.INVISIBLE);
        RegisterDriverButton.setEnabled(false);

        CreateDriverAccount.setOnClickListener(view -> {
            CreateDriverAccount.setVisibility(View.INVISIBLE);
            LoginDriverButton.setVisibility(View.INVISIBLE);

            TitleDriver.setText("Driver Registration");

            RegisterDriverButton.setVisibility(View.VISIBLE);
            RegisterDriverButton.setEnabled(true);
        });


        RegisterDriverButton.setOnClickListener(view -> {
            String email = driverEmail.getText().toString();
            String password = driverPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
            } else {
                loadingBar.setTitle("Please wait :");
                loadingBar.setMessage("While system is performing processing on your data...");
                loadingBar.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onlineDriverID = mAuth.getCurrentUser().getUid();

                        driversDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(onlineDriverID);
                        driversDatabaseRef.setValue(true);

                        Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapsActivity.class);
                        startActivity(intent);

                        loadingBar.dismiss();
                    } else {
                        Toast.makeText(DriverLoginRegisterActivity.this, "Please Try Again. Error Occurred, while registering... ", Toast.LENGTH_SHORT).show();

                        loadingBar.dismiss();
                    }
                });
            }
        });


        LoginDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = driverEmail.getText().toString();
                String password = driverPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Please wait :");
                    loadingBar.setMessage("While system is performing processing on your data...");
                    loadingBar.show();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Sign In , Successful...", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapsActivity.class);
                                startActivity(intent);
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Error Occurred, while Signing In... ", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();

                            }
                        }
                    });
                }
            }
        });
    }


  //  @Override
  //  protected void onStart() {
  //      super.onStart();
  //      mAuth.addAuthStateListener(firebaseAuthListner);
  //  }
//
  //  @Override
  //  protected void onStop() {
  //      super.onStop();
  //      mAuth.removeAuthStateListener(firebaseAuthListner);
  //  }
}