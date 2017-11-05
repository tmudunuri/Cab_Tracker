package com.bmsit.marzil.driverapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;


    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRootReference=firebaseDatabase.getReference();
    private DatabaseReference mTypeReference=mRootReference.child("drivers");
    private DatabaseReference mLocReference;
    private RiderDataClass data;
    private Marker driver;
    private Marker rider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent=getIntent();
        String varname=intent.getStringExtra("regpass");
        mLocReference=mTypeReference.child(varname);
        final DecimalFormat format=new DecimalFormat("#0.0000");
        driver.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cabicon));
        rider.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.usericon));

        mRootReference.child("riders").orderByChild("cab").equalTo(varname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    RiderDataClass data= snapshot.getValue(RiderDataClass.class);
                    Toast.makeText(DriverMapActivity.this, data.cab, Toast.LENGTH_LONG).show();
                    rider=mMap.addMarker(new MarkerOptions().position(new LatLng(data.lat,data.lng)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude=location.getLatitude();
                    mLocReference.child("lat").setValue(latitude);
                    double longitude=location.getLongitude();
                    mLocReference.child("lng").setValue(longitude);
                    Geocoder geocoder=new Geocoder(getApplicationContext());


                    LatLng driverloc=new LatLng(latitude,longitude);
                    try {
                        List<Address> addresses=
                                geocoder.getFromLocation(latitude,longitude,1);

                        if(driver==null)
                                driver=mMap.addMarker(new MarkerOptions().position(driverloc));
                        else
                            {
                                driver.setPosition(driverloc);
                            }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverloc,12));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }






    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
