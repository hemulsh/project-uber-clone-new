package com.example.privatasaot1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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
    private String onlineCustomerID;
    private FirebaseUser currentUser;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);


        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = firebaseAuth -> {
            currentUser = mAuth.getCurrentUser();
            if (currentUser != null){
                Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
           startActivity(intent);
            }
        };


        customerLoginBtn = findViewById(R.id.customer_button_login);
        customerRegisterBtn = findViewById(R.id.customer_button_register);
        customerRegisterLink = findViewById(R.id.customer_register_link);
        customerStatus = findViewById(R.id.customer_status);

        emailCustomer = findViewById(R.id.email_customer);
        passwordCustomer = findViewById(R.id.password_customer);


        loadingBar = new ProgressDialog(this);

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
            String email = emailCustomer.getText().toString();
            String password = passwordCustomer.getText().toString();


            registerCustomer(email, password);// livdok metoda zot
        }));

        customerLoginBtn.setOnClickListener((v -> {
            String email = emailCustomer.getText().toString();
            String password = passwordCustomer.getText().toString();

            signInCustomer(email, password);
        }));

    }

    //bemetoda zo ma kore kaasher lohzim al lahzan signIn
    private void signInCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write Password", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Customer login");
            loadingBar.setMessage("Please wait, while we are chicking your credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login Successfully...", Toast.LENGTH_SHORT).show();
                            Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
                            startActivity(customerIntent);

                            loadingBar.dismiss();

                        } else {
                            Toast.makeText(CustomerLoginRegisterActivity.this, "Login Unsuccessful, Please try Again...", Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();
                        }
                    });
        }


    }


    //bemetoda ma kore kaasher lohzim al kaftor button
    private void registerCustomer(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write Password", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Please wait");
            loadingBar.setMessage("While system is performing processing on your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            currentUserId = mAuth.getCurrentUser().getUid();
                            customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(currentUserId);
                            customerDatabaseRef.setValue(true);

                            Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login Successfully...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();

                        } else {
                            Toast.makeText(CustomerLoginRegisterActivity.this, "Login Unsuccessful, Please try Again...", Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();
                        }
                    });
        }
    }
}
