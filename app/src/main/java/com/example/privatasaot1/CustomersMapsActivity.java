package com.example.privatasaot1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import com.google.firebase.database.core.Context;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomersMapsActivity extends FragmentActivity implements OnMapReadyCallback,
       GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,

        com.google.android.gms.location.LocationListener {



    private GoogleMap mMap;

    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;



    private Button logoutCustomerBtn;
    private Button settingsCustomerBtn;
    private Button callCarBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private LatLng customerPickUpLocation;

    private DatabaseReference driverAvailableRef, driverLocationRef, driversRef, customerDatabaseRef;
    private int radius = 1;

    private Boolean driverFound = false, requestType = false;
    private String driverFoundID;
    private String customerID;
    Marker driverMarker, pickUpMarker;
    GeoQuery geoQuery;
    GeoFire geoFire;

    private ValueEventListener driverLocationRefListener;


    private TextView txtName, txtPhone, txtCarName;
    private CircleImageView profilePic;
    private ConstraintLayout relativeLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_maps);
        FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {

            //TODO:  meathel et amufa(mehaber aplikacia le Firebase)
            //FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth = FirebaseAuth.getInstance();

            //TODO: methaber le currentuser beFirebas
            // (FirebaseUser curentUser = FirebaseAuth.getInstance().getCurrentUser();)
            currentUser = mAuth.getCurrentUser();

            //TODO: bodek aim currentUser kayam lifnei shemoshkhim oto meFirebase ki im hu null ihie krisa
            if (currentUser != null) {
                //TODO: im meshtamesh kayam, mosheh oto leaplikaciya kidei leavod ito(leishtamesh bo)
                customerID = mAuth.getCurrentUser().getUid();
            }else {
                //kofecet odaa im meshtamesh lo kayam
                Toast.makeText(CustomersMapsActivity.this, "meshtamesh lo kayam", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(CustomersMapsActivity.this, CustomerLoginRegisterActivity.class);
           startActivity(intent);
            }
        };




        //TODO: DataBaseReference custumerDatabaseRef; methaber le dataBase shel Firebase kidei
        // leavod ito o leahnis bo masheu
        // (dataBaseReference = ssilka na bazu danih v Firebase)

        //TODO: (Точка входа для доступа к базе данных Firebase. Вы можете получить экземпляр,
        // позвонив getInstance().Чтобы получить доступ к местоположению в базе данных и прочитать
        // или записать данные, используйте getReference().)
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests");
        driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        logoutCustomerBtn = findViewById(R.id.customer_logout_button);
        settingsCustomerBtn = findViewById(R.id.customer_settings_button);
        callCarBtn = findViewById(R.id.call_driver_button);

        txtName = findViewById(R.id.name_driver);
        txtPhone = findViewById(R.id.phone_driver);
        txtCarName = findViewById(R.id.car_name);
        profilePic = findViewById(R.id.profile_image_driver);
        relativeLayout = findViewById(R.id.rel1);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
         if (mapFragment != null) {
        mapFragment.getMapAsync(this);
         }else {
             Toast.makeText(this, "connect to the local", Toast.LENGTH_SHORT).show();
         }

            //lambda- lebicua peula kol shehi betoh lambda:
          settingsCustomerBtn.setOnClickListener(view -> {
              Intent intent = new Intent(this, SettingsActivity.class);
              intent.putExtra("type", "Customers");
              startActivity(intent);
          });


        logoutCustomerBtn.setOnClickListener(v -> {

            logoutUser();

            //menatek meshtamesh betoh
            mAuth.signOut();



        });

        callCarBtn.setOnClickListener((view -> {

            if (requestType) {

                requestType = false;
                geoQuery.removeAllListeners();
                driverLocationRef.removeEventListener(driverLocationRefListener);


                if (driverFound != null) {
                    driversRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Drivers").child(driverFoundID).child("CustomerRideID");

                    driversRef.removeValue();
                    driverFoundID = null;
                }

                driverFound = false;
                radius = 1;

                GeoFire geoFire = new GeoFire(customerDatabaseRef);
                geoFire.removeLocation(customerID);

                if (pickUpMarker != null) pickUpMarker.remove();

                if (driverMarker != null) driverMarker.remove();

                callCarBtn.setText("Call a Driver");
                relativeLayout.setVisibility(View.GONE);

            } else {

                requestType = true;


                GeoFire geoFire = new GeoFire(customerDatabaseRef);
                geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));


                customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                
               try {
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPickUpLocation).title("My Location")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_new_location)));
                    callCarBtn.setText("Getting your Driver...");
                    getClosetDriverCab();


                }catch (Exception e){
                   System.out.println(e);
                    Toast.makeText(this, "pickupmarer null(lama ze kaha pizdec!!!)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }));
    }


   /* метод ищет водителя поблизости с клиентом */
    private void getClosetDriverCab() {

        GeoFire geoFire = new GeoFire(driverAvailableRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude, customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //anytime the driver is called this method will be called
                //key=DriverID and the location

                if (!driverFound && requestType) {
                    driverFound = true;
                    driverFoundID = key;

                    //we tell driver which customer he is going to have
                    driversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    HashMap driversMap = new HashMap();
                    driversMap.put("CustomerRideID", customerID);
                    driversRef.updateChildren(driversMap);

                    //show driver location on CustomerMapActivity
                    gettingDriverLocation();
                    callCarBtn.setText("Looking for Driver Location...");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!driverFound) {
                    radius = radius + 1;
                    getClosetDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    //and then we get to the driver location - to tell customer where is the driver
    private void gettingDriverLocation() {
        driverLocationRefListener = driverLocationRef.child(driverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && requestType) {

                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double locationLat = 0;
                            double locationLng = 0;
                            callCarBtn.setText("Driver Found");

                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInformation();

                            if (driverLocationMap.get(0) != null) {
                                locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }

                            if (driverLocationMap.get(1) != null) {
                                locationLng = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            //adding marker - to pointing where driver is - using this lat lng
                            LatLng driverLatLng = new LatLng(locationLat, locationLng);

                            if (driverMarker != null) driverMarker.remove();

                            Location location1 = new Location("");
                            location1.setLatitude(customerPickUpLocation.latitude);
                            location1.setLongitude(customerPickUpLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(customerPickUpLocation.latitude);
                            location2.setLongitude(customerPickUpLocation.longitude);

                            float distance = location1.distanceTo(location2);

                            if (distance < 90) {
                                callCarBtn.setText("Driver's Reached");
                            } else {

                                callCarBtn.setText("Driver Found: " + String.valueOf(distance));

                            }

                            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng)
                                    .title("your driver is here")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_new_driver_car)));
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
        //now le set user location enabled
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

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
        //it will handle the refreshment of the location
        //if we dont call it we will get location only once

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        //getting the updated location

        // if (getApplicationContext() != null) { // ezlo ein bdika shel if livdok yoter meuhar

      //  System.out.println("location zarih leagia");
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

    //create this method for using apis

    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }



    @Override
    protected void onStop() {
        super.onStop();
    }

    private void logoutUser() {
        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);

        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(welcomeIntent);
        finish();
    }



    private void  getAssignedDriverInformation(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverFoundID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);


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






