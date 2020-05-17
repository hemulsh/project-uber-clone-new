package com.example.privatasaot1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button welcomeDriverBtn;
    private Button welcomeCustomerBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeDriverBtn = findViewById(R.id.welcome_button_driver);
        welcomeCustomerBtn = findViewById(R.id.welcom_button_customer);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        welcomeCustomerBtn.setOnClickListener((view) -> {
            Intent customerLoginActivity = new Intent(this, CustomerLoginRegisterActivity.class);
            startActivity(customerLoginActivity);
        });

        welcomeDriverBtn.setOnClickListener((v -> {
            Intent driverLoginActivity = new Intent(this, DriverLoginRegisterActivity.class);
            startActivity(driverLoginActivity);
        }));

    }


}
