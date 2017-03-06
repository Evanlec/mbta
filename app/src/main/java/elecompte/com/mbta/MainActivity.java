package elecompte.com.mbta;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.List;

import elecompte.com.mbta.api.MbtaApi;
import elecompte.com.mbta.model.AlertHeader;
import elecompte.com.mbta.model.Direction;
import elecompte.com.mbta.model.Mode;
import elecompte.com.mbta.model.PredictionsByStop;
import elecompte.com.mbta.model.Route;
import elecompte.com.mbta.model.Stop;
import elecompte.com.mbta.model.StopsByRoute;
import elecompte.com.mbta.model.Trip;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private int mInterval = 10000;
    private Handler mHandler;
    private Runnable httpRunner;

    public List<String> stops;
    public List<String> apiStops;
    public List<Stop> stopList;

    private static final String PREFERENCES_STOP = "stop";
    private static final String API_KEY = "wX9NwuHnZU2ToO7GmGR9uw";
    private static final String FORMAT = "json";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    public MbtaApi api;

    public ProgressBar networkSyncIcon;
    private TextView northboundView;
    private TextView northboundView2;
    private TextView southboundView;
    private TextView southboundView2;
    private TextView alerts;


    private Spinner stopSelector;
    private String selectedStop;

    private String tripTemplate;
    private String alertTemplate;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("onConnectionSuspended", i + "");
    }

    @Override
    public void onConnectionFailed(ConnectionResult r) {
        Log.e("onConnectionFailed", "Failed to connect to Google Play Services: " + r.getErrorMessage());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("onConnected", "onConnected");
        if (!checkFineLocationPermission()) {
            Log.w("onConnected", "lacking fine location permission!");
            Log.i("onConnected", "asking for permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLocation();
        }

    }

    private void getLocation() {
        Log.d("getLocation", "fetching location");
        if (!checkFineLocationPermission()) {
            return;
        }

        final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Log.d("Latitude", mLastLocation.getLatitude() + "");
            Log.d("Longitude", mLastLocation.getLongitude() + "");
            api.getStopsByRoute(API_KEY, "Red", FORMAT, new Callback<StopsByRoute>() {
                @Override
                public void success(StopsByRoute stops, Response response) {
                    Direction northDirection = stops.getDirectionByDirectionId("0");
                    stopList = northDirection.stops;
                    Stop stop = StopsByRoute.getNearestStop(stopList, mLastLocation);
                    if (stop != null) {
                        Log.d("getStopsByRoute", "Found nearest stop: " + stop.getParentStationName());
                        try {
                            stopSelector.setSelection(apiStops.indexOf(stop.getParentStation()));
                            Toast.makeText(getApplicationContext(), "Determined nearest station: " + stop.getParentStationName(), Toast.LENGTH_SHORT).show();
                            return;
                        } catch (IndexOutOfBoundsException e) {
                            Log.e("getStopsByRoute", "Failed to set station");
                            return;
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Failed to locate nearest station", Toast.LENGTH_SHORT).show();
                    stopSelector.setSelection(0);
                }
                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "Failed to locate nearest station", Toast.LENGTH_SHORT).show();
                    Log.e("getLocation", "Error getting stops");
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    stopSelector.setSelection(0);
                    Toast.makeText(getApplicationContext(), "Failed to locate nearest station", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = Access.getInstance().getMbtaApi();

        // Create an instance of GoogleAPIClient.
        Log.d("onCreate", "onCreate");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        setContentView(R.layout.activity_main);

        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (NullPointerException e) {
            Log.e("onCreate", "Null pointer exception: " + e);
        }
        getSupportActionBar().setIcon(R.drawable.app_logo);

        networkSyncIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        networkSyncIcon.setVisibility(View.INVISIBLE);
        stopSelector = (Spinner) findViewById(R.id.stopSelector);
        northboundView = (TextView) findViewById(R.id.textViewNorthboundValue);
        northboundView2 = (TextView) findViewById(R.id.textViewNorthboundValue2);
        southboundView = (TextView) findViewById(R.id.textViewSouthboundValue);
        southboundView2 = (TextView) findViewById(R.id.textViewSouthboundValue2);
        alerts = (TextView) findViewById(R.id.textViewAlerts);

        stopSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                northboundView.setText("");
                northboundView2.setText("");
                southboundView.setText("");
                southboundView2.setText("");
                httpRunner.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        tripTemplate = getString(R.string.trip_template);
        alertTemplate = getString(R.string.alerts_template);

        stops = Arrays.asList(getResources().getStringArray(R.array.stop_list));
        apiStops = Arrays.asList(getResources().getStringArray(R.array.api_stop_list));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stops);

        // set the view for the Drop down list
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set the ArrayAdapter to the spinner
        stopSelector.setAdapter(dataAdapter);
        stopSelector.setSelection(0);

        selectedStop = apiStops.get(stopSelector.getSelectedItemPosition());


        mHandler = new Handler();
        httpRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("httpRunner", "show network icon");
                    networkSyncIcon.setVisibility(View.VISIBLE);
                    getPredictions(selectedStop);
                } finally {
                    mHandler.removeCallbacks(this);
                    mHandler.postDelayed(httpRunner, mInterval);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        Log.i("onStart", "onStart: " + mGoogleApiClient.isConnecting());
        super.onStart();
        httpRunner.run();
    }

    @Override
    protected void onStop() {
        Log.d("onStop", "onStop");
        mGoogleApiClient.disconnect();
        super.onStop();
        mHandler.removeCallbacks(httpRunner);
    }

    private void getPredictions(String stop) {
        assert northboundView != null;

        api.getPredictionsByStop(API_KEY, stop, FORMAT, new Callback<PredictionsByStop>() {
            @Override
            public void success(PredictionsByStop predictionsByStop, Response response) {
                Log.d("httpRunner", "hide network icon");
                networkSyncIcon.setVisibility(View.INVISIBLE);

                String alertText = "";

                if (predictionsByStop == null) {
                    northboundView.setText(R.string.no_data);
                    return;
                }

                // see if there are any alerts
                if (predictionsByStop.alertHeaders != null) {
                    for (AlertHeader h : predictionsByStop.alertHeaders) {
                        alertText = alertText.concat(String.format("%s -- %s \n", h.effectName, h.headerText));
                    }
                    alerts.setText(String.format(alertTemplate, alertText));
                }

                Mode mode = predictionsByStop.getModeByType("1");
                if (mode == null) {
                    northboundView.setText(R.string.no_data);
                    southboundView.setText(R.string.no_data);
                    return;
                }

                Route route = mode.getRouteById("Red");

                // northbound
                Direction northDirection = route.getDirectionByDirectionId("1");

                if (northDirection == null) {
                    northboundView.setText(R.string.no_data);
                    return;
                } else {
                    List<Trip> northTrips = northDirection.getNearestTrips();
                    String northPrediction = northTrips.get(0).getPreAwayFormatted();
                    northboundView.setText(String.format(tripTemplate, northTrips.get(0).tripHeadsign.toUpperCase(), northPrediction));
                    // we might not have more than 1 trip, so use try/catch here
                    try {
                        String northPrediction2 = northTrips.get(1).getPreAwayFormatted();
                        northboundView2.setText(String.format(tripTemplate, northTrips.get(1).tripHeadsign.toUpperCase(), northPrediction2));
                    } catch (IndexOutOfBoundsException e) {
                        northboundView2.setText(R.string.no_data);
                        return;
                    }

                }

                // southbound
                Direction southDirection = route.getDirectionByDirectionId("0");

                if (southDirection == null) {
                    southboundView.setText(R.string.no_data);
                } else {
                    List<Trip> southTrips = southDirection.getNearestTrips();
                    String southPrediction = southTrips.get(0).getPreAwayFormatted();
                    southboundView.setText(String.format(tripTemplate, southTrips.get(0).tripHeadsign, southPrediction));
                    try {
                        String southPrediction2 = southTrips.get(1).getPreAwayFormatted();
                        southboundView2.setText(String.format(tripTemplate, southTrips.get(1).tripHeadsign, southPrediction2));
                    } catch (IndexOutOfBoundsException e) {
                        southboundView2.setText(R.string.no_data);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("httpRunner", "hide network icon");
                networkSyncIcon.setVisibility(View.INVISIBLE);
                northboundView.setText(String.format("Error: %s", error.toString()));
                southboundView.setText(String.format("Error: %s", error.toString()));
            }
        });

    }

    /**
     * Check if the app has access to fine location permission. On pre-M
     * devices this will always return true.
     */
    private boolean checkFineLocationPermission() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Store preferred stop in app preferences
     */
    private void storePreferredStop(String stop) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFERENCES_STOP, stop);
        editor.apply();
    }

    /**
     * Retrieve preferred stop
     */
    private String getPreferredStop() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(PREFERENCES_STOP, "place-andrw");
    }

}
