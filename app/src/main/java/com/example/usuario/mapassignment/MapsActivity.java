package com.example.usuario.mapassignment;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnCameraChangeListener{

    //The code to get my current location was taken from a teamtreehouse's blog
    //http://blog.teamtreehouse.com/beginners-guide-location-android

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private EditText mTextContent;
    private LatLngBounds mLatLngBounds;
    private List<MarkerOptions> mMarkerOptions;
    private List<Circle> mCircle;
    public static final String TAG = FragmentActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Initialize List

        mMarkerOptions = new ArrayList<MarkerOptions>();
        mCircle = new ArrayList<Circle>();

        setUpMapIfNeeded();

        //Initialize our client

        mTextContent = (EditText) findViewById(R.id.textContent);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        //Listener for long click on map
        mMap.setOnMapLongClickListener(this);

//        Context context = (Activity) this;
//        SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);



        //Clean the old Preferences

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        //Set the onMoveCameraListener

        mMap.setOnCameraChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMarkerOptions.add(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(700) //meters
                .strokeColor(Color.RED);

        Circle circle = mMap.addCircle(circleOptions);

        mCircle.add(circle);

        //Save the first marker
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("lat",currentLatitude+"");
        editor.putString("lng",currentLongitude+"");
        editor.putString("content","I am here!");

//        Set markers = new HashSet();
        //markers.add(latLng);
//        markers.add(currentLatitude);
//        markers.add(currentLongitude);
//        markers.add("I am here!");
//        editor.putStringSet("markers",markers);
        editor.apply();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        String content = mTextContent.getText().toString();
        if (!content.trim().equals("")) {

            double lat = latLng.latitude;
            double lng = latLng.longitude;

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(content);
            mMap.addMarker(options);
            mMarkerOptions.add(options);
            //Set the Circles for each marker

            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .radius(0)      //meters
                    .strokeColor(Color.RED);

            Circle circle = mMap.addCircle(circleOptions);

            mCircle.add(circle);
            //SharedPreferences

            //Taken from http://deepak-sharma.net/2013/11/20/how-to-get-set-values-in-sharedpreferences-in-android/

            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lat", lat + "");
            editor.putString("lng", lng + "");
            editor.putString("content", content);

//        Set markers = new HashSet();
//        //markers.add(latLng);
//        markers.add(lat);
//        markers.add(lng);
//        markers.add(content);
//        editor.putStringSet("markers",markers);
            Boolean flag = editor.commit();
            if (flag) {
                Toast.makeText(getApplicationContext(), "The Marker was saved successfully!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(MapsActivity.this, "The content can't be blank.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mLatLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        updateCircles(mLatLngBounds);
    }

    public void updateCircles(LatLngBounds latLngBounds){

        //Taken from http://stackoverflow.com/questions/17735812/how-can-i-get-the-visible-markers-in-google-maps-v2-in-android
        //Get the values of the borders

        double lowLat;
        double lowLng;
        double highLat;
        double highLng;

        if (latLngBounds.northeast.latitude < latLngBounds.southwest.latitude)
        {
            lowLat = latLngBounds.northeast.latitude;
            highLat = latLngBounds.southwest.latitude;
        }
        else
        {
            highLat = latLngBounds.northeast.latitude;
            lowLat = latLngBounds.southwest.latitude;
        }
        if (latLngBounds.northeast.longitude < latLngBounds.southwest.longitude)
        {
            lowLng = latLngBounds.northeast.longitude;
            highLng = latLngBounds.southwest.longitude;
        }
        else
        {
            highLng = latLngBounds.northeast.longitude;
            lowLng = latLngBounds.southwest.longitude;
        }

        for (int i = 0; i < mMarkerOptions.size(); i++){
            MarkerOptions marker = mMarkerOptions.get(i);
            Double markerLat = marker.getPosition().latitude;
            Double markerLng = marker.getPosition().longitude;

            Circle mapCircle = mCircle.get(i);

//            Circle mapCircle;
//            CircleOptions circleOptions = new CircleOptions()
//                    .center(marker.getPosition())
//                    .radius(1000); //meters
//
//            mapCircle = mMap.addCircle(circleOptions);

            if (markerLat <= highLat && markerLat >= lowLat && markerLng <= highLng && markerLng >= lowLng){

                Log.d(TAG, "Lat: "+ markerLat + " Lng: "+ markerLng);

                //Erase the circle around the marker


                if (mapCircle != null){
                    Log.d(TAG, "Radius Before :" + mapCircle.getRadius());
                    mapCircle.setRadius(0);
                    Log.d(TAG, "Radius" + mapCircle.getRadius());
                }
            }
            else{

                double distance;
                int divValue = 8;

                //Spherical Util: https://developers.google.com/maps/documentation/android/utility/

                //1
                if (markerLat > highLat && markerLng < lowLng){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(highLat-(highLat-lowLat)/divValue,lowLng+(highLng-lowLng)/divValue));
                }
                //2
                else if (markerLat > highLat && (markerLng >= lowLng && markerLng <= highLng)){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(highLat-(highLat-lowLat)/divValue,markerLng));
                }
                //3
                else if (markerLat > highLat && markerLng > highLng){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(highLat-(highLat-lowLat)/divValue,highLng-(highLng-lowLng)/divValue));
                }
                //4
                else if ((markerLat <= highLat && markerLat >= lowLat) && markerLng < lowLng ){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(markerLat,lowLng+(highLng-lowLng)/divValue));
                }
                //5
                else if ((markerLat <= highLat && markerLat >= lowLat) && markerLng > highLng){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(markerLat,highLng-(highLng-lowLng)/divValue));
                }
                //6
                else if (markerLat < lowLat && markerLng < lowLng){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(lowLat+(highLat-lowLat)/divValue,lowLng+(highLng-lowLng)/divValue));
                }
                //7
                else if (markerLat < lowLat && (markerLng >= lowLng && markerLng <= highLng)){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(lowLat+(highLat-lowLat)/divValue,markerLng));
                }
                //8
                else if (markerLat < lowLat && markerLng > highLng){
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(lowLat+(highLat-lowLat)/divValue,highLng-(highLng-lowLng)/divValue));
                }
                else{
                    distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), new LatLng(highLat-(highLat-lowLat)/divValue,highLng-(highLng-lowLng)/divValue));
                }

                if (mapCircle != null){
                    Log.d(TAG, "Radius Before :" + mapCircle.getRadius());
                    mapCircle.setRadius(distance);
                    Log.d(TAG, "Radius" + mapCircle.getRadius());
                }
            }
        }
    }
}
