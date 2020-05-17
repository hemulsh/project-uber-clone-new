package com.example.privatasaot1;

import android.Manifest;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
        import android.widget.Button;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.constraintlayout.widget.ConstraintLayout;
        import androidx.core.app.ActivityCompat;
        import androidx.fragment.app.FragmentActivity;

        import com.firebase.geofire.GeoFire;
        import com.firebase.geofire.GeoLocation;
        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.logging.Logger;
import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;
        import com.squareup.picasso.Picasso;

        import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class DriversMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private Button logoutDriverBtn;
    private Button settingsDriverBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Boolean currentLogoutDriverStatus = false;

    private DatabaseReference assignedCustomerRef, assignedCustomerPickUpRef;
    private ValueEventListener assignedCustomerPickUpRefListener;

    private String driverID, customerID = "";
    Marker pickUpMarker;

    private TextView txtName, txtPhone;
    private CircleImageView profilePic;
    private ConstraintLayout relativeLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_maps);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        try {
            if (currentUser != null) driverID = currentUser.getUid();
        } catch (Exception e) {
            System.out.println("Can't init user " + e.getMessage() );
            Log.e("System Error", e.getMessage());
            Toast.makeText(DriversMapsActivity.this, "Can't init user " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        logoutDriverBtn = findViewById(R.id.driver_logout_button);
        settingsDriverBtn = findViewById(R.id.driver_settings_button);



        txtName = findViewById(R.id.name_customer);
        txtPhone = findViewById(R.id.phone_customer);
        profilePic = findViewById(R.id.profile_image_customer);
        relativeLayout = findViewById(R.id.rel2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
       if (mapFragment != null) {
            mapFragment.getMapAsync(this);



            settingsDriverBtn.setOnClickListener(view -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra("type", "Drivers");
                startActivity(intent);
            });

            logoutDriverBtn.setOnClickListener(v -> {

                currentLogoutDriverStatus = true;
               disconnectTheDriver();
                mAuth.signOut();

                signOutDriver();

            });
        }

        getAssignCustomerRequest();
    }

    private void getAssignCustomerRequest() {

        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("CustomerRideID");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    customerID = dataSnapshot.getValue().toString();

                    //getting assigned customer location
                    getAssignCustomerPickUpLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();

                }else {
                    customerID = "";
                    if (pickUpMarker != null){

                        pickUpMarker.remove();
                    }

                    if (assignedCustomerPickUpRefListener != null){
                        assignedCustomerPickUpRef.removeEventListener(assignedCustomerPickUpRefListener);
                    }

                    relativeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignCustomerPickUpLocation() {

        assignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests")
                .child(customerID).child("l");

        assignedCustomerPickUpRefListener = assignedCustomerPickUpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> customerLocationMap = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;


                    if (customerLocationMap.get(0) != null){
                        locationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) != null){
                        locationLng = Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);

                    if(pickUpMarker != null){
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Customer PickUp Location").icon(BitmapDescriptorFactory
                                    .fromResource(R.drawable.user)));
                }else {
                        Log.e("ERROR", "msg");
                        Log.i("information", "can't set puckup bitma");
                        //Toast.makeText(DriversMapsActivity.this, "ein pikupmarker", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buidGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext() != null){

            //getting the updated location
            lastLocation = location;
                                        //location                //location
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


            DatabaseReference driverAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireAvailability = new GeoFire(driverAvailibilityRef);

            DatabaseReference driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireWorking = new GeoFire(driverWorkingRef);

            switch (customerID){
                case "":
                    geoFireWorking.removeLocation(driverID);

                    geoFireAvailability.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));

                    break;

                default:
                    geoFireAvailability.removeLocation(driverID);
                    geoFireWorking.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));

                    break;
            }

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }




    protected synchronized void buidGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (!currentLogoutDriverStatus){
            disconnectTheDriver();
        }

    }


    private void disconnectTheDriver() {
//        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        GeoFire geoFire = new GeoFire(driverAvailibilityRef);
        geoFire.removeLocation(driverID);
    }

    private void signOutDriver() {
        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);

        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(welcomeIntent);

        finish();
    }

    private void  getAssignedCustomerInformation(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();


                    txtName.setText(name);
                    txtPhone.setText(phone);



                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
