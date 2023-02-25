package com.example.etaka;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LocationBasedAnalysis extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Marker currentLocationMarker;
    FusedLocationProviderClient fusedLocationClient;
    Button confirm;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_based_analysis);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        confirm = (Button) findViewById(R.id.btn_cnfrm);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            if (currentLocationMarker != null) currentLocationMarker.remove();
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                        }
                    }
                });
        confirm.setOnClickListener(v -> {
            confirm.setText("Please Wait");
            confirm.setEnabled(false);
//            Intent i = new Intent(LocationBasedAnalysis.this, LocationAnalysis.class);
//            i.putExtra("LATITUDE", currentLocationMarker.getPosition().latitude);
//            i.putExtra("LONGITUDE", currentLocationMarker.getPosition().longitude);
//            startActivity(i);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.plc_hldr, new FragmentLoading()) // Add the fragment to the placeholder
                    .commit();
            MakeAPICall();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (currentLocationMarker != null) currentLocationMarker.remove();
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Land Location"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            } else {
// Location permission denied, show an error message
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void MakeAPICall(){
        double latitude = currentLocationMarker.getPosition().latitude;

        double longitude = currentLocationMarker.getPosition().longitude;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://192.168.135.148:5000/post?Lat=" + Double.toString(latitude) + "&Long=" + Double.toString(longitude) + "&date=2022-07-01&end_dt=2022-07-30";
        //String URL = "https://soil-health.herokuapp.com/post?Lat=19.704656&Long=74.248489&date=2022-07-01&end_dt=2022-07-30";
        JSONObject jsonBody = new JSONObject();
        final String mRequestBody = jsonBody.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LOG_VOLLEY", response);
                Toast.makeText(LocationBasedAnalysis.this, "Report Received", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LocationBasedAnalysis.this, LocationAnalysis.class);
                i.putExtra("RESPONSE", response);
                i.putExtra("LATITUDE", currentLocationMarker.getPosition().latitude);
                i.putExtra("LONGITUDE", currentLocationMarker.getPosition().longitude);
                startActivity(i);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LOG_VOLLEY", error.toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(LocationBasedAnalysis.this);
                builder.setTitle("Try Again");
                builder.setMessage("Failed to generate a report. Check if location entered is not an urban area");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onBackPressed();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(LocationBasedAnalysis.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }
}