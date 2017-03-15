package elecompte.com.mbta;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import elecompte.com.mbta.api.MbtaApi;
import elecompte.com.mbta.model.Direction;
import elecompte.com.mbta.model.Mode;
import elecompte.com.mbta.model.Route;
import elecompte.com.mbta.model.Stop;
import elecompte.com.mbta.model.StopsByRoute;
import elecompte.com.mbta.model.Trip;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private int mInterval = 10000;
    private Handler mHandler;
    private Runnable httpRunner;

    public List<String> stops;
    public List<String> apiStops;
    public List<Stop> stopList;
    private Observable<StopsByRoute> stopsByRouteObservable;

    private static final String API_KEY = "wX9NwuHnZU2ToO7GmGR9uw";
    private static final String FORMAT = "json";
    private static final String ROUTE_REDLINE = "Red";
    private static final String MODE_SUBWAY = "1";
    private static final String DIRECTION_NORTH = "1";
    private static final String DIRECTION_SOUTH = "0";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    public MbtaApi api;
    private String alertTemplate;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private List<Trip> northTrips = new ArrayList<>();
    private List<Trip> southTrips = new ArrayList<>();

    public ProgressBar networkSyncIcon;
    private TextView alerts;
    private ArrayAdapter northboundAdapter;
    private ArrayAdapter southboundAdapter;
    private Spinner stopSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("onCreate", "onCreate");

        api = Access.getInstance().getMbtaApi();
        stopsByRouteObservable = api.getStopsByRoute(API_KEY, ROUTE_REDLINE, FORMAT);
        northboundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, northTrips);
        southboundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, southTrips);


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.app_logo);

        networkSyncIcon = (ProgressBar) findViewById(R.id.loadingIcon);
        networkSyncIcon.setVisibility(View.INVISIBLE);
        stopSelector = (Spinner) findViewById(R.id.stopSelector);
        alerts = (TextView) findViewById(R.id.textViewAlerts);

        ListView northboundListView = (ListView) findViewById(R.id.northboundListView);
        northboundListView.setAdapter(northboundAdapter);

        ListView southboundListView = (ListView) findViewById(R.id.southboundListView);
        southboundListView.setAdapter(southboundAdapter);


        alertTemplate = getString(R.string.alerts_template);

        stops = Arrays.asList(getResources().getStringArray(R.array.stop_list));
        apiStops = Arrays.asList(getResources().getStringArray(R.array.api_stop_list));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_text, stops);

        // set the view for the Drop down list
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stopSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                httpRunner.run();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // set the ArrayAdapter to the spinner
        stopSelector.setAdapter(dataAdapter);
        stopSelector.setSelection(0);

        mHandler = new Handler();
        httpRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    networkSyncIcon.setVisibility(View.VISIBLE);
                    getPredictions(apiStops.get(stopSelector.getSelectedItemPosition()));
                } finally {
                    mHandler.removeCallbacks(this);
                    mHandler.postDelayed(httpRunner, mInterval);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        clearDisplay();
        Log.i("onStart", "mGoogleApiClient.isConnected(): " + mGoogleApiClient.isConnected());
        httpRunner.run();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop", "onStop");
        mHandler.removeCallbacks(httpRunner);
    }

    @Override
    protected void onDestroy() {
        Log.d("onDestroy", "");
        super.onDestroy();
    }

    private void getPredictions(String stop) {
        clearDisplay();
        Log.d("getPredictions", mHandler.toString());

        api.getPredictionsByStop(API_KEY, stop, FORMAT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(predictionsByStop -> {
                    Log.d("getPredictionsByStop", "stop: " + stop);
                    // see if there are any alerts
                    if (predictionsByStop.alertHeaders != null) {
                        alerts.setText(String.format(alertTemplate, predictionsByStop.getAlertHeadersConcat()));
                    }

                    Mode mode = predictionsByStop.getModeByType(MODE_SUBWAY);
                    if (mode == null) { return; }
                    Route route = mode.getRouteById(ROUTE_REDLINE);

                    // northbound
                    Direction northDirection = route.getDirectionByDirectionId(DIRECTION_NORTH);
                    if (northDirection != null) {
                        northTrips.clear();
                        northTrips.addAll(northDirection.getNearestTrips());
                        northboundAdapter.notifyDataSetChanged();
                        Log.d("getPredictionsByStop", "northTrips: " + northTrips);
                    }

                    // southbound
                    Direction southDirection = route.getDirectionByDirectionId(DIRECTION_SOUTH);
                    if (southDirection != null) {
                        southTrips.clear();
                        southTrips.addAll(southDirection.getNearestTrips());
                        Log.d("getPredictionsByStop", "southTrips: " + southTrips);
                        southboundAdapter.notifyDataSetChanged();
                    }

                    networkSyncIcon.setVisibility(View.INVISIBLE);
        }, error -> {
                    Log.e("getPredictions()", "Error: " + error);
                    networkSyncIcon.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("onConnectionSuspended", i + "");
    }

    @Override
    public void onConnectionFailed(ConnectionResult r) {
        Log.e("onConnectionFailed", "Failed to connect to Google Play Services: " + r.getErrorMessage());
        locationFailedNotify();
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
        if (!checkFineLocationPermission()) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            locationFailedNotify();
            return;
        }
        Log.d("getLocation()", "Latitude: " + mLastLocation.getLatitude());
        Log.d("getLocation()", "Longitude: " + mLastLocation.getLongitude());

        mGoogleApiClient.disconnect();
        getNearestStop();
    }

    private void getNearestStop() {
        Log.d("getNearestStop", "getNearestStop()");
        stopsByRouteObservable
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(stopsByRoute ->  Observable.fromIterable(stopsByRoute.direction))
                .filter(direction -> direction.directionId.equals(DIRECTION_SOUTH))
                .map(direction -> direction.stops)
                .cache()
                .subscribe(stops -> {
                            stopList = stops;
                            Log.d("stopsByRouteObservable", "stopList set" + stopList);
                            Stop stop = StopsByRoute.getNearestStop(stopList, mLastLocation);
                            if (stop != null) {
                                Log.d("getNearestStop", "Found nearest stop: " + stop.getParentStationName());
                                stopSelector.setSelection(apiStops.indexOf(stop.getParentStation()));
                                Toast.makeText(getApplicationContext(), "Found nearest station: " + stop.getParentStationName(), Toast.LENGTH_SHORT).show();
                            }
                        }, error -> {
                            Log.e("getStops", "Error getting stops: " + error);
                            Toast.makeText(getApplicationContext(), "Failed to fetch stops", Toast.LENGTH_SHORT).show();
                        }
                );

    }

    private void locationFailedNotify() {
        Toast.makeText(getApplicationContext(), "Failed to locate nearest station", Toast.LENGTH_SHORT).show();
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
                    locationFailedNotify();
                }
            }
        }
    }

    private boolean checkFineLocationPermission() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    protected void clearDisplay() {
        alerts.setText("");
        northboundAdapter.clear();
        southboundAdapter.clear();
    }

}
