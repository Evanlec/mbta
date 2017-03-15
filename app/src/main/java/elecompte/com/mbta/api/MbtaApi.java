package elecompte.com.mbta.api;

import elecompte.com.mbta.model.PredictionsByStop;
import elecompte.com.mbta.model.StopsByRoute;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MbtaApi {
    @GET("predictionsbystop")
    Observable<PredictionsByStop> getPredictionsByStop(
            @Query("api_key") String apiKey,
            @Query("stop") String stop,
            @Query("format") String format
    );
    @GET("stopsbyroute")
    Observable<StopsByRoute> getStopsByRoute(
            @Query("api_key") String apiKey,
            @Query("route") String route,
            @Query("format") String format
    );
}
