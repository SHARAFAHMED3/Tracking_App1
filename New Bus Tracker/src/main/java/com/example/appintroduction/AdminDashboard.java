package com.example.appintroduction;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;
public class AdminDashboard extends AppCompatActivity {
    private ValueEventListener valueEventListener;
    private GoogleMap mMap;
    private Handler mHandler = new Handler();
    private Marker mMarker;
    private LocationManager manager;
    private com.google.android.gms.maps.model.LatLng LatLng;
    private Set<Marker> markerSet = new HashSet<>();
    private EditText etMessage;
    private Button btnSend;
    Button buttonlogout;
    private DatabaseReference messagesRef;
    private Button btnDelete;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean zoomFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnDelete = findViewById(R.id.btnDelete);
        buttonlogout = findViewById(R.id.btnlogoutadm);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Loginpage.class);
            startActivity(intent);
            finish();
        } else {
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
        }
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                } else {
                    Toast.makeText(AdminDashboard.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog();
            }

        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                // Add a marker in Akkaraippattu and move the camera
                LatLng Akkaraippattu = new LatLng(7.217935774319166, 81.8498394498399);
                mMap.addMarker(new MarkerOptions().position(Akkaraippattu).title("Akkaraippattu Bus Station"));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Akkaraippattu, 15));
                mToastRunnable.run();
            }
        });
    }

    private void sendMessage(String message) {
        if (auth.getCurrentUser() != null) {
            String userEmail = auth.getCurrentUser().getEmail();
            String messageId = messagesRef.push().getKey();

            UserMessage newMessage = new UserMessage(userEmail, message);
            messagesRef.child(messageId).setValue(newMessage);

            Toast.makeText(AdminDashboard.this, "Message sent", Toast.LENGTH_SHORT).show();
            etMessage.setText("");
        } else {
            // Handle the case when the current user is null
            Toast.makeText(AdminDashboard.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Confirmation");
        builder.setMessage("Are you sure you want to delete all messages?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllMessages();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAllMessages() {
        if (messagesRef != null) {
            messagesRef.removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error == null) {
                        Toast.makeText(AdminDashboard.this, "All messages deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminDashboard.this, "Failed to delete messages", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            messagesRef.removeEventListener(valueEventListener);
        }
    }

     //the Show all buses
    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
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
                        zoomFlag = true; // Set the flag to indicate that the operation has been performed
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Failed in database", Toast.LENGTH_SHORT).show();
                }
            });
            mHandler.postDelayed(this, 1000);
        }
    };

}
