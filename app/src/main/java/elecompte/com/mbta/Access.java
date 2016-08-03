package elecompte.com.mbta;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import elecompte.com.mbta.api.MbtaApi;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

public class Access {

    private static final int TIMEOUT = 15; // Seconds
    private static final RestAdapter.LogLevel LOGLEVEL = RestAdapter.LogLevel.BASIC;
    private static final String TAG = "Access";


    private static Access instance;

    private OkClient okClient;
    private JacksonConverter converter;

    private MbtaApi mbtaApi;

    /**
     * For thread-safe approach, double checking singleton instance.
     *
     * @return instance
     */
    public static Access getInstance() {
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
        // Make HTTP client
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.setFollowRedirects(false);
        okClient = new OkClient(okHttpClient);

        // Make JSON converter
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converter = new JacksonConverter(mapper);
    }


    public MbtaApi getMbtaApi() {
        if (mbtaApi != null) return (mbtaApi);

        RestAdapter adapter = getRestAdapter("http://realtime.mbta.com");
        mbtaApi = adapter.create(MbtaApi.class);

        return (mbtaApi);
    }

    public RestAdapter getRestAdapter(String endpoint) {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(okClient);

        if (endpoint == null) return (null);

        builder.setEndpoint(endpoint);

        builder.setConverter(converter);

        builder.setLogLevel(LOGLEVEL);
        builder.setLog(new AndroidLog(TAG));

        return builder.build();
    }


}
