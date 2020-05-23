package com.example.privatasaot1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;
import java.util.regex.Pattern;

public class CustomerLoginRegisterActivity extends AppCompatActivity {


    private TextView customerRegisterLink;
    private TextView customerStatus;
    private Button customerLoginBtn;
    private Button customerRegisterBtn;
    private EditText emailCustomer;
    private EditText passwordCustomer;

    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference customerDatabaseRef;
    private FirebaseUser currentUser;
    private String onlineCustomerID;


    OnSuccessListener<AuthResult> mSuccessListener = new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            if (loadingBar.isShowing()) {
                loadingBar.dismiss();
            }

            onlineCustomerID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlineCustomerID);
            customerDatabaseRef.setValue(true);


            Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
            CustomerLoginRegisterActivity.this.startActivity(intent);
            finish();


        }
    };
    // metoda onFailureListener=
    OnFailureListener mFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            if (loadingBar.isShowing()) {
                loadingBar.dismiss();
            }

            Toast.makeText(CustomerLoginRegisterActivity.
                    this, "Login Unsuccessful, Please try Again...", Toast.
                    LENGTH_SHORT).show();

            showError(e);
        }
    };

    //new my
    private void showError(Exception e) {
        new AlertDialog.Builder(this).setTitle("An Error Occurred").
                setMessage(e.getLocalizedMessage()).setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);


        mAuth = FirebaseAuth.getInstance();

      // firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
      //     @Override
      //     public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
      //         currentUser = mAuth.getCurrentUser();
      //         if (currentUser != null) {
      //             Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
      //             CustomerLoginRegisterActivity.this.startActivity(intent);
      //             finish();
      //         }
      //     }
      // };


        customerLoginBtn = findViewById(R.id.customer_button_login);
        customerRegisterBtn = findViewById(R.id.customer_button_register);
        customerRegisterLink = findViewById(R.id.customer_register_link);
        customerStatus = findViewById(R.id.customer_status);

        emailCustomer = findViewById(R.id.email_customer);
        passwordCustomer = findViewById(R.id.password_customer);

        customerRegisterBtn.setVisibility(View.INVISIBLE);
        customerRegisterBtn.setEnabled(false);


        customerRegisterLink.setOnClickListener((v -> {
            customerLoginBtn.setVisibility(View.INVISIBLE);
            customerRegisterLink.setVisibility(View.INVISIBLE);
            customerStatus.setText("Driver Registration");
            customerRegisterBtn.setVisibility(View.VISIBLE);
            customerRegisterBtn.setEnabled(true);
        }));

        customerRegisterBtn.setOnClickListener((v -> {

            register();
        }));

        customerLoginBtn.setOnClickListener((v -> {

            login();
        }));

    }

    //new my
    private void register() {
        String email = getEmail();
        String password = getPassword();

        //dont continue if the details are not valid:
        if (email == null || password == null) {
            return;
        }

        showProgress();

        //register-> addOnSuccess -> addOnFailure
        mAuth.createUserWithEmailAndPassword(email, password).
                addOnSuccessListener(mSuccessListener).
                addOnFailureListener(mFailureListener);
    }

    //new my
    private void login() {
        String email = getEmail();
        String password = getPassword();

        //dont continue if the details are not valid:
        if (email == null || password == null) {
            return;
        }
        showProgress();

        //register-> addOnSuccess -> addOnFailure
        mAuth.signInWithEmailAndPassword(email, password).
                addOnSuccessListener(mSuccessListener).
                addOnFailureListener(mFailureListener);
    }

    //new my
    private String getEmail() {
        String email = emailCustomer.getText().toString();

        //Patterns.EMAIL_ADDRESS = mean check if email address is valid!!
        ///you can learn about patterns in google with search ="regex pattern"
        Pattern emailAddressRegex = Patterns.EMAIL_ADDRESS;
        boolean isEmailValid = emailAddressRegex.matcher(email).matches();
        if (!isEmailValid) {
            emailCustomer.setError("Invalid email address");
            //return null if email not valid
            return null;
        }
        return email;
    }

    //new my
    private String getPassword() {
        String pass = passwordCustomer.getText().toString();

        if (pass.length() < 4) {
            passwordCustomer.setError("Password must contain at last 4 characters");
            //return null if password not valid
            return null;
        }
        return pass;
    }

    private void showProgress() {
        if (loadingBar == null) {
            loadingBar = new ProgressDialog(this);
            loadingBar.setTitle("Please wait");
            loadingBar.setMessage("While system is performing processing on your data...");
        }
        loadingBar.show();
    }

}
