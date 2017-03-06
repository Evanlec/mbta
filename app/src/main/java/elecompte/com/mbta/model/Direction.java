
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Direction {

    @JsonProperty("direction_id")
    public String directionId;
    @JsonProperty("direction_name")
    public String directionName;
    @JsonProperty("trip")
    public List<Trip> trip = new ArrayList<>();

    @JsonProperty("stop")
    public List<Stop> stops = new ArrayList<>();


    public List<Trip> getNearestTrips() {
        Collections.sort(trip);
        return trip;
    }


}
