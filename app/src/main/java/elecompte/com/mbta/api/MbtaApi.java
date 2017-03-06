package elecompte.com.mbta.api;

import elecompte.com.mbta.model.PredictionsByStop;
import elecompte.com.mbta.model.StopsByLocation;
import elecompte.com.mbta.model.StopsByRoute;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface MbtaApi {
    @GET("/developer/api/v2/predictionsbystop")
    void getPredictionsByStop(
            @Query("api_key") String apiKey,
            @Query("stop") String stop,
            @Query("format") String format,
            Callback<PredictionsByStop> callback
    );
    @GET("/developer/api/v2/stopsbylocation")
    void getStopsByLocation(
            @Query("api_key") String apiKey,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("format") String format,
            Callback<StopsByLocation> callback
    );
    @GET("/developer/api/v2/stopsbyroute")
    void getStopsByRoute(
            @Query("api_key") String apiKey,
            @Query("route") String route,
            @Query("format") String format,
            Callback<StopsByRoute> callback
    );

}
