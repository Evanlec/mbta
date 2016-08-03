package elecompte.com.mbta;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import elecompte.com.mbta.api.MbtaApi;
import elecompte.com.mbta.model.AlertHeader;
import elecompte.com.mbta.model.Direction;
import elecompte.com.mbta.model.Mode;
import elecompte.com.mbta.model.PredictionsByStop;
import elecompte.com.mbta.model.Route;
import elecompte.com.mbta.model.Trip;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private int mInterval = 15000;
    private Handler mHandler;
    private Runnable httpRunner;

    public List<String> stops;
    public List<String> apiStops;

    public static final String API_KEY = "wX9NwuHnZU2ToO7GmGR9uw";
    public static final String STOP = "place-andrw";
    //public static final String STOP = "place-knncl";
    public static final String FORMAT = "json";

    public MbtaApi api;

    private TextView northboundView;
    private TextView northboundView2;
    private TextView southboundView;
    private TextView southboundView2;
    private TextView alerts;


    private Spinner stopSelector;

    private String tripTemplate;
    private String alertTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.app_logo);

        northboundView = (TextView) findViewById(R.id.textViewNorthboundValue);
        northboundView2 = (TextView) findViewById(R.id.textViewNorthboundValue2);
        southboundView = (TextView) findViewById(R.id.textViewSouthboundValue);
        southboundView2 = (TextView) findViewById(R.id.textViewSouthboundValue2);
        alerts = (TextView) findViewById(R.id.textViewAlerts);
        stopSelector = (Spinner) findViewById(R.id.stopSelector);

        stopSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                httpRunner.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
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


        mHandler = new Handler();
        httpRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    getPredictions(apiStops.get(stopSelector.getSelectedItemPosition()));
                } finally {
                    mHandler.removeCallbacks(this);
                    mHandler.postDelayed(httpRunner, mInterval);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        httpRunner.run();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(httpRunner);
    }

    private void getPredictions(String stop) {
        if (stop == null) {
            stop = "place-andrw";
        }
        api = Access.getInstance().getMbtaApi();
        assert northboundView != null;

        api.getPredictionsByStop(API_KEY, stop, FORMAT, new Callback<PredictionsByStop>() {
            @Override
            public void success(PredictionsByStop predictionsByStop, Response response) {
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
                    northboundView.setText(String.format(tripTemplate, northTrips.get(0).tripHeadsign, northPrediction));
                    // we might not have more than 1 trip, so use try/catch here
                    try {
                        String northPrediction2 = northTrips.get(1).getPreAwayFormatted();
                        northboundView2.setText(String.format(tripTemplate, northTrips.get(1).tripHeadsign, northPrediction2));

                    } catch (IndexOutOfBoundsException e) {
                        northboundView.setText(R.string.no_data);
                        return;
                    }

                }

                // southbound
                Direction southDirection = route.getDirectionByDirectionId("0");

                if (southDirection == null) {
                    southboundView.setText(R.string.no_data);
                    return;
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
                northboundView.setText(String.format("Error: %s", error.toString()));
                southboundView.setText(String.format("Error: %s", error.toString()));

            }
        });

    }

}
