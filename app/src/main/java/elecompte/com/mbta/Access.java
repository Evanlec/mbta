package elecompte.com.mbta;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import elecompte.com.mbta.api.MbtaApi;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

class Access {

    private static final int TIMEOUT = 5; // Seconds
    private static final String TAG = "HTTP";

    private ObjectMapper mapper = new ObjectMapper();
    private static Access instance;
    private MbtaApi mbtaApi;

    /**
     * For thread-safe approach, double checking singleton instance.
     *
     * @return instance
     */
    static Access getInstance() {
        if (instance == null) {
            synchronized (Access.class) {
                // Double check
                if (instance == null) {
                    instance = new Access();
                }
            }
        }
        return (instance);
    }

    private Access() {
        // Make JSON converter
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient.Builder okClientBuilder = new OkHttpClient.Builder();
        okClientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.readTimeout(TIMEOUT, TimeUnit.SECONDS);
        okClientBuilder.addInterceptor(interceptor);
        return okClientBuilder.build();
    }

    MbtaApi getMbtaApi() {
        if (mbtaApi != null) return (mbtaApi);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://realtime.mbta.com/developer/api/v2/")
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        mbtaApi = retrofit.create(MbtaApi.class);

        return (mbtaApi);
    }


}
