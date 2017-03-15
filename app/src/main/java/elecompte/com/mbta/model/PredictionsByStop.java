
package elecompte.com.mbta.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class PredictionsByStop {

    @JsonProperty("stop_id")
    public String stopId;
    @JsonProperty("stop_name")
    public String stopName;
    @JsonProperty("mode")
    public List<Mode> mode = new ArrayList<>();
    @JsonProperty("alert_headers")
    public List<AlertHeader> alertHeaders = new ArrayList<>();

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public List<Mode> getModes() {
        return mode;
    }

    public Mode getModeByType(String type) {
        for (Mode m : mode) {
            if (m.routeType.equals(type)) {
                return m;
            }
        }
        return null;
    }

    public String getAlertHeadersConcat() {
        return TextUtils.join("\n", this.alertHeaders);
    }


}
