package com.appbootup.findme;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class FindMeMapsActivity extends FragmentActivity implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minute
    private final static String API_KEY = "722bb384-578f-417d-bf12-0b36050862dd";
    final String TAG = "com.appbootup.findme.FindMeMapsActivity";
    boolean gpsEnabledFl = false;
    boolean networkEnabledFl = false;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;
    private int cid, lac, psc, mcc, mnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_me_maps);
        setUpTelephonyManager();
        setUpMapIfNeeded();
        setupLocationManager();
        Application.getEventBus().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpTelephonyManager();
        setUpMapIfNeeded();
        setupLocationManager();
        Application.getEventBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getEventBus().unregister(this);
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
        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    private void setupLocationManager() {

        if (mLocationManager == null) {
            // Get LocationManager object from System Service LOCATION_SERVICE
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            // Get the name of the best provider
            String provider = mLocationManager.getBestProvider(criteria, true);
            // Get Current Location
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                showLocationOnMap(lastKnownLocation);
            }
            // exceptions will be thrown if provider is not permitted.
            try {
                gpsEnabledFl = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                Log.e(TAG, "GPS_PROVIDER", e);
            }

            try {
                networkEnabledFl = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                Log.e(TAG, "NETWORK_PROVIDER", e);
            }
            if (!gpsEnabledFl && !networkEnabledFl) {
                showToast("LOCATION PROVIDER not found!");
                this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }

            // if gps is enabled, get location updates
            if (gpsEnabledFl) {
                Log.e(TAG, "GPS Enabled, requesting updates.");
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }

            // if network is enabled, get location updates
            if (networkEnabledFl) {
                Log.e(TAG, "Network Enabled, requesting updates.");
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
        }
    }

    private void setUpTelephonyManager() {
        //CID, MNC, MCC, LAC ==>  http://opencellid.org/ or http://www.cell2gps.com
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        cid = cellLocation.getCid();
        lac = cellLocation.getLac();
        psc = cellLocation.getPsc();

        Log.e(TAG, "cid = " + cid + ",lac = " + lac + ",psc = " + psc);

        /*
        * Mcc and mnc is concatenated in the networkOperatorString. The first 3
        * chars is the mcc and the last 2 is the mnc.
        */
        String networkOperator = telephonyManager.getNetworkOperator();
        if (networkOperator != null && networkOperator.length() > 0) {
            try {
                int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                int mnc = Integer.parseInt(networkOperator.substring(3));
            } catch (NumberFormatException e) {
            }
        }

        String message = "CellID: " + cid;
        message = message + "\n" + "Lac: " + lac;
        message = message + "\n" + "Mcc: " + mcc;
        message = message + "\n" + "Mnc: " + mnc;
        showToast(message);

        /**
         * Seems that cid and lac shall be in hex. Cid should be padded with zero's
         * to 8 numbers if UMTS (3G) cell, otherwise to 4 numbers. Mcc padded to 3
         * numbers. Mnc padded to 2 numbers.
         */
        try {
            findLocationByCellInfo(cid, lac, mnc, mcc);
        } catch (IOException e) {
            Log.e(TAG, "findLocationByCellInfo failed." + e);
        }
    }

    private void showToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private void showLocationOnMap(Location location) {
        // Get latitude of the current location
        double latitude = location.getLatitude();
        // Get longitude of the current location
        double longitude = location.getLongitude();
        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!").snippet("Consider yourself located"));
    }

    @Override
    public void onLocationChanged(Location location) {
        showLocationOnMap(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, provider + " onStatusChanged - " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled - " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled - " + provider);
    }

    private void findLocationByCellInfo(int cid, int lac, int mnc, int mcc)
            throws IOException {
        try {

            String sampleUrl = "http://opencellid.org/cell/get?key=" + API_KEY + "&mcc=" + mcc + "&mnc=" + mnc + "&lac=" +
                    lac + "&cellid=" + cid + "&format=json";
            String baseUrl = "http://opencellid.org";
            Log.d(TAG, "Fetched : " + sampleUrl);

            RequestInterceptor requestInterceptor = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("User-Agent", "Retrofit-OpenCellIdService");
                }
            };

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Date.class, new DateTypeAdapter())
                    .create();

            RestAdapter openCellIdServiceRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(baseUrl)
                    .setRequestInterceptor(requestInterceptor)
                    .setConverter(new GsonConverter(gson))
                    .build();

            OpenCellIdService openCellIdService = openCellIdServiceRestAdapter.create(OpenCellIdService.class);


            new OpenCellIdRestService()
                    .fetch(openCellIdService, baseUrl,
                            cid, lac, mnc, mcc);


            // OpenCell openCell = openCellIdService.getCell(API_KEY, mcc, mnc, lac, cid, "json");
            // onOpenCells(openCell);

        } catch (Exception e) {
            Log.e(TAG, "Incorrect Location processing.", e);
        }
    }

    @Subscribe
    public void onOpenCells(OpenCell openCell) {
        String message = "Longitude: "
                + openCell.getLon();
        message = message + "\n" + "Latitude: "
                + openCell.getLat();
        message = message + "\n" + "Name: "
                + openCell.getNid();
        message = message + "\n" + "Accuracy: "
                + openCell.getAverageSignalStrength();
        showToast(message);
    }
}