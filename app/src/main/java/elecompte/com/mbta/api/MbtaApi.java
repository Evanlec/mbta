package elecompte.com.mbta.api;

import elecompte.com.mbta.model.PredictionsByStop;
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

}
