
package elecompte.com.mbta.model;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class StopsByRoute {

    @JsonProperty("route_name")
    public String routeName;

    @JsonProperty("direction")
    public List<Direction> direction;

    public static Stop getNearestStop(List<Stop> stops, Location lastLocation) {
        if (stops != null) {
            for (Stop stop : stops) {
                Location l = new Location("none");
                l.setLatitude(stop.getStopLat());
                l.setLongitude(stop.getStopLon());
                stop.setDistance(lastLocation.distanceTo(l));
            }
            Collections.sort(stops);
            return stops.get(0);
        }
        return null;
    }
}
