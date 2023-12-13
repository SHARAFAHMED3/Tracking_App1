package com.example.appintroduction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DriverDashboard extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<UserMessage> messageList;
    private DatabaseReference messagesRef;
    private LocationRequest locationRequest;
    private boolean isGpsOn = false;
    private Handler mHandler = new Handler();
    private SwitchCompat toggleButton;
    FirebaseAuth auth;
    FirebaseUser user;
    Button buttonlogout, buttonGoMap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);
        toggleButton = findViewById(R.id.switchButton);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        auth = FirebaseAuth.getInstance();
        buttonlogout = findViewById(R.id.btnlogoutdvr);
        buttonGoMap = findViewById(R.id.buttonGoMap);

        recyclerView = findViewById(R.id.listMessages);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        buttonlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                mHandler.removeCallbacks(mToastRunnable);
                Intent intent = new Intent(getApplicationContext(), Loginpage.class);
                startActivity(intent);
                finish();
            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    mToastRunnable.run();
                } else if (!compoundButton.isChecked()) {
                    mHandler.removeCallbacks(mToastRunnable);
                }
            }
        });

        buttonGoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapPage();
            }
        });

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
                // Handle updated
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Handle removed message
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle moved message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    // calling method to update location in every 5 seconds.
    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userEmail = user.getEmail();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // user permission
                    if (ActivityCompat.checkSelfPermission(DriverDashboard.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // GPS is turn on or not
                        if (isGPSEnabled()) {

                            LocationServices.getFusedLocationProviderClient(DriverDashboard.this)
                                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                                        @Override
                                        public void onLocationResult(@NonNull LocationResult locationResult) {
                                            super.onLocationResult(locationResult);

                                            LocationServices.getFusedLocationProviderClient(DriverDashboard.this)
                                                    .removeLocationUpdates(this);

                                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                                int index = locationResult.getLocations().size() - 1;
                                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                                double longitude = locationResult.getLocations().get(index).getLongitude();

                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userEmail.replace('.', ','));
                                                // Check if email exists in Firebase under "Users" node
                                                DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Users");
                                                adminRef.child(userEmail.replace('.', ',')).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            // Email exists, update the latitude and longitude
                                                            DatabaseReference emailRef = adminRef.child(userEmail.replace('.', ','));
                                                            LocationHelper helper = new LocationHelper(longitude, latitude);
                                                            emailRef.setValue(helper);
                                                        } else {
                                                            // Email doesn't exist, create a new entry
                                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userEmail.replace('.', ','));
                                                            LocationHelper helper = new LocationHelper(longitude, latitude);
                                                            ref.setValue(helper);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Handle database error
                                                    }
                                                });
                                            }
                                        }
                                    }, Looper.getMainLooper());
                        } else {
                            turnOnGPS();
                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                }
            }
            mHandler.postDelayed(this, 500);
        }
    };

    // turnOnGPS
    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(DriverDashboard.this, 2);
                            } catch (IntentSender.SendIntentException sendIntentException) {
                                sendIntentException.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });
    }

    // isGPSEnabled
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnabled) {
            isGpsOn = true;
            return true;
        } else {
            Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Open Map Page
    private void openMapPage() {
        Intent intent = new Intent(DriverDashboard.this, BusCurrentLocation.class);
        startActivity(intent);
    }
}
