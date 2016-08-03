
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Mode {

    @JsonProperty("route_type")
    public String routeType;
    @JsonProperty("mode_name")
    public String modeName;
    @JsonProperty("route")
    public List<Route> route = new ArrayList<>();

    public Route getRouteById(String id) {
        for (Route r : route) {
            if (r.routeId.equals(id)) {
                return r;
            }
        }
        return null;
    }

}
