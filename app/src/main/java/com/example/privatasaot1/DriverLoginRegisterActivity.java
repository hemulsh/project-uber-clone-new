package com.example.privatasaot1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Pattern;

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
    private FirebaseAuth.AuthStateListener firebaseAuthListner;

    private ProgressDialog loadingBar;
    private FirebaseUser currentUser;
    String onlineDriverID;
    //TODO: check the currentUer
    OnSuccessListener<AuthResult> mSuccesListener = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {

            if (loadingBar.isShowing()) {
                loadingBar.dismiss();
            }
            onlineDriverID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            driversDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(onlineDriverID);
            driversDatabaseRef.setValue(true);

            Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapsActivity.class);
            DriverLoginRegisterActivity.this.startActivity(intent);
            finish();

        }
    };
    OnFailureListener mFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

            if (loadingBar.isShowing()) {
                loadingBar.dismiss();
            }

            Toast.makeText(DriverLoginRegisterActivity.this, "Login Unsuccessful, Please try Again...", Toast.
                    LENGTH_SHORT).show();

            showError(e);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);


        mAuth = FirebaseAuth.getInstance();

        CreateDriverAccount = findViewById(R.id.register_link_driver);
        TitleDriver = findViewById(R.id.driver_status);
        LoginDriverButton = findViewById(R.id.driver_button_login);
        RegisterDriverButton = findViewById(R.id.driver_button_register);
        driverEmail = findViewById(R.id.email_driver);
        driverPassword = findViewById(R.id.password_driver);

        RegisterDriverButton.setVisibility(View.INVISIBLE);
        RegisterDriverButton.setEnabled(false);

        CreateDriverAccount.setOnClickListener(view -> {
            CreateDriverAccount.setVisibility(View.INVISIBLE);
            LoginDriverButton.setVisibility(View.INVISIBLE);

            TitleDriver.setText("Driver Registration");

            RegisterDriverButton.setVisibility(View.VISIBLE);
            RegisterDriverButton.setEnabled(true);
        });

        firebaseAuthListner = new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = getInstance().getCurrentUser();
                if (currentUser != null) {
                    Intent intent = new Intent(DriverLoginRegisterActivity.this, DriversMapsActivity.class);
                    DriverLoginRegisterActivity.this.startActivity(intent);
                    finish();
                }
            }
        };


        RegisterDriverButton.setOnClickListener(view -> {

            register();

        });


        LoginDriverButton.setOnClickListener(view -> {

            login();

        });
    }

    //new my
    private void register() {
        String email = getEmail();
        String password = getPassword();

        //dont continue if the details are not valid:
        if (email == null || password == null) {
            return;
        }

        shoProgress();

        //register -> addOnsuccess -> addOnFailure
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(mSuccesListener)
                .addOnFailureListener(mFailureListener);
    }


    //new my
    private String getEmail() {
        String email = driverEmail.getText().toString();

        //class Pattern can check if  email or ip address or num of tel
        Pattern emailAddressRegex = Patterns.EMAIL_ADDRESS;
        //mather(email).matches()= take the email and check him in a Pattern
        boolean isEmailValid = emailAddressRegex.matcher(email).matches();

        if (!isEmailValid) {
            driverEmail.setError("Invalid email address");
            return null;
        }
        return email;
    }

    //new my
    private String getPassword() {

        String pass = driverPassword.getText().toString();

        if (pass.length() < 4) {
            driverPassword.setError("Password must contain at least 4 character");
            return null;
        }
        return pass;
    }

    //new my
    private void login() {

        String email = getEmail();
        String password = getPassword();

        //dont continue if the details are not valid:
        if (email == null || password == null) {
            return;
        }
        shoProgress();

        //signIn -> addOnSuccess -> addOnFailure
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(mSuccesListener)
                .addOnFailureListener(mFailureListener);
    }

    //new my
    private void shoProgress() {
        //before use if its null -> init
        //lazy-variable:(lazy-loading design-pattern)
        if (loadingBar == null) {
            loadingBar = new ProgressDialog(this);
            loadingBar.setTitle("Please Wait...");
            loadingBar.setMessage("Logging you in");
        }
        loadingBar.show();

    }

    //new my
    private void showError(Exception e) {
        new AlertDialog.Builder(this).setTitle("An Error Occurred").
                setMessage(e.getLocalizedMessage()).setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

}