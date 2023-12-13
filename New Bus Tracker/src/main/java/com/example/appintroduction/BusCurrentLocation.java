package com.example.appintroduction;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.appintroduction.databinding.ActivityBusCurrentLocationBinding;
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

public class BusCurrentLocation extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Handler mHandler = new Handler();
    private ActivityBusCurrentLocationBinding binding;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBusCurrentLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        mapFragment.getMapAsync(this);
        mToastRunnable.run();
    }
    // Display Current Location of Own Bus
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getEmail().replace('.', ','));
    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                MyLocation location = snapshot.getValue(MyLocation.class);
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    if (mMarker != null) {
                                        mMarker.remove();
                                    }
                                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("My Bus");
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.nbrmarker));
                                    mMarker= mMap.addMarker(markerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                }
                            } catch (Exception e) {
                                Toast.makeText(BusCurrentLocation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Akkaraippattu and move the camera
        LatLng Akkaraippattu = new LatLng(7.217935774319166, 81.8498394498399);
        mMap.addMarker(new MarkerOptions().position(Akkaraippattu).title("Akkaraippattu Bus Station"));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Akkaraippattu, 19));
    }

}
