package com.example.etaka;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationAnalysis extends AppCompatActivity implements OnMapReadyCallback {
    private static final int PERMISSION_REQUEST_CODE = 201;
    TextView resTxt;
    private SupportMapFragment mapFragment;
    LatLng latLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_analysis);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        String res =getIntent().getStringExtra("RESPONSE");
//        if(res != null){
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        Bundle bundle = new Bundle();
        bundle.putString("RESPONSE", res);
        if(!checkPermission()){
            requestPermission();
        }
        AnalysisHome analysisHome = new AnalysisHome();
        analysisHome.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .replace(R.id.data_place_holder, analysisHome) // Add the fragment to the placeholder
                        .commit();
//        }
        latLng = new LatLng(getIntent().getDoubleExtra("LATITUDE",0), getIntent().getDoubleExtra("LONGITUDE", 0));

    }
    public boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        if (googleMap != null) {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
        }
    }
}