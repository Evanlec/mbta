
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class Direction {

    @JsonProperty("direction_id")
    public String directionId;
    @JsonProperty("direction_name")
    public String directionName;
    @JsonProperty("trip")
    public List<Trip> trips;
    @JsonProperty("stop")
    public List<Stop> stops;

    public List<Trip> getNearestTrips() {
        if (trips != null) {
            Collections.sort(trips);
            return trips;
        }
        return null;
    }
}
