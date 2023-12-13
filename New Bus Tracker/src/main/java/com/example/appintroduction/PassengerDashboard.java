package com.example.appintroduction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PassengerDashboard extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference locationRef;
    private ValueEventListener valueEventListener;
    private Set<Marker> markerSet = new HashSet<>();
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<UserMessage> messageList;
    private DatabaseReference messagesRef;
    private GoogleMap mMap;
    Button buttonlogout, btnMyLocation;
    EditText etSearch;
    Spinner spinnerOptions;
    Button btnSearch;
    FirebaseAuth auth;
    FirebaseUser user;
    private Handler mHandler = new Handler();
    private Marker mMarker,mMarker1;
    private LocationManager manager;
    private com.google.android.gms.maps.model.LatLng LatLng;
    private String userValue;
    private List<String> optionsList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private boolean isShowingAllBuses = false;
    private boolean zoomFlag,zoomFlagSelected,zoomFlagUser = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_dashboard);

        auth = FirebaseAuth.getInstance();
        buttonlogout = findViewById(R.id.btnlogout);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        spinnerOptions = findViewById(R.id.spinnerOptions);
        btnSearch = findViewById(R.id.btnSearch);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // massages
        recyclerView = findViewById(R.id.listMessages);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Loginpage.class);
            startActivity(intent);
            finish();
        }
        buttonlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                mHandler.removeCallbacks(mToastRunnable);
                mHandler.removeCallbacks(mToastRunnable1);
                Intent intent = new Intent(getApplicationContext(), Loginpage.class);
                startActivity(intent);
                finish();
            }
        });
        // Set click listeners for feature buttons
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if location permission is granted
                if (ContextCompat.checkSelfPermission(PassengerDashboard.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Start location updates
                    requestLocationUpdates();
                } else {
                    // Request location permission
                    ActivityCompat.requestPermissions(PassengerDashboard.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        });

        // Set click listener for search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedOption = spinnerOptions.getSelectedItem().toString();
                if (selectedOption.equals("Akkaraipattu to Batticaloa")) {
                    // Handle individual bus location
                    isShowingAllBuses = false;
                    mHandler.removeCallbacks(mToastRunnable1);
                    ref = FirebaseDatabase.getInstance().getReference("Users").child("sharaf@gmail,com");
                    mToastRunnable.run();
                } else if (selectedOption.equals("Akkaraipattu to Kalmunai")) {
                    // Handle individual bus location
                    isShowingAllBuses = false;
                    mHandler.removeCallbacks(mToastRunnable1);
                    ref = FirebaseDatabase.getInstance().getReference("Users").child("testuser@gmail,com");
                    mToastRunnable.run();
                } else if (selectedOption.equals("Akkaraipattu to Colombo")) {
                    // Handle individual bus location
                    isShowingAllBuses = false;
                    mHandler.removeCallbacks(mToastRunnable1);
                    ref = FirebaseDatabase.getInstance().getReference("Users").child("driver11@gamil,com");
                    mToastRunnable.run();
                } else if (selectedOption.equals("Show All Buses")) {
                    isShowingAllBuses = !isShowingAllBuses; // Toggle the value
                    if (isShowingAllBuses) {
                        mHandler.removeCallbacks(mToastRunnable); // Stop showing individual bus
                        mToastRunnable1.run(); // Start showing all buses
                    } else {
                        mHandler.removeCallbacks(mToastRunnable1); // Stop showing all buses
                    }
                }
            }
        });
        // Create and set the spinner adapter
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOptions.setAdapter(spinnerAdapter);
        // options dynamically
        addOption("Show All Buses");
        addOption("Akkaraipattu to Batticaloa");
        addOption("Akkaraipattu to Kalmunai");
        addOption("Akkaraipattu to Colombo");

        // message display on dashboard
        // Initialize Firebase
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        // Retrieve messages from Firebase Database
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                UserMessage message = snapshot.getValue(UserMessage.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle updated message if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed message if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle moved message if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                // Add a marker in Defualt and move the camera
                LatLng Akkaraippattu = new LatLng(7.217935774319166, 81.8498394498399);
                mMap.addMarker(new MarkerOptions().position(Akkaraippattu).title("Akkaraippattu Bus Station"));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Akkaraippattu, 15));
            }
        });
    }
    // show selected bus
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowingAllBuses) {
                return; // Stop executing if showing all buses
            }
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        double latitude = snapshot.child("latitude").getValue(Double.class);
                        double longitude = snapshot.child("longitude").getValue(Double.class);

                        mMap.clear(); // Clear previous markers
                        LatLng latLng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Bus Location");
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.nbrmarker));
                        Marker marker = mMap.addMarker(markerOptions);
                        if (!zoomFlagSelected) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                            zoomFlagSelected = true;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            mHandler.postDelayed(this, 1000);
        }
    };
    // option method
    private void addOption(String option) {
        optionsList.add(option);
        spinnerAdapter.notifyDataSetChanged();
    }
// user current location
    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // Update location every 5 seconds (adjust as needed)
                .setFastestInterval(2000); // Fastest update interval

        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMarker(latLng);
                }
            }
        };
        // Check location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void updateMarker(LatLng latLng) {
        // Remove previous marker (if exists)

            if (mMarker1 != null) {
                mMarker1.remove();
            }
            // Add a new marker for the user's location
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Location");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
            mMarker1 = mMap.addMarker(markerOptions);
        if (!zoomFlagUser) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            zoomFlagUser = true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                requestLocationUpdates();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Check if location permission is granted and start location updates
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

// show all buses
private Runnable mToastRunnable1 = new Runnable() {
        @Override
        public void run() {
            if (!isShowingAllBuses) {
                return; // Stop executing if not showing all buses
            }
            DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Users");
            locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mMap.clear(); // Clear previous markers
                    markerSet.clear(); // Clear the marker set

                    for (DataSnapshot emailSnapshot : snapshot.getChildren()) {
                        String email = emailSnapshot.getKey();
                        double latitude = emailSnapshot.child("latitude").getValue(Double.class);
                        double longitude = emailSnapshot.child("longitude").getValue(Double.class);

                        LatLng latLng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(email);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.nbrmarker));
                        Marker marker = mMap.addMarker(markerOptions);
                        markerSet.add(marker); // Add the marker to the set
                    }
                    // Zoom and move camera to fit all markers
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markerSet) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();

                    if (!zoomFlag) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        zoomFlag = true;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            mHandler.postDelayed(this, 2000);
        }
    };

}
