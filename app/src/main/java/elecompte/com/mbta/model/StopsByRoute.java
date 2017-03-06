
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

   public Direction getDirectionByDirectionId(String id) {
        for (Direction d : direction) {
            if (d.directionId.equals(id)) {
                return d;
            }
        }
        return null;
    }

    public static Stop getNearestStop(List<Stop> stops, Location location) {

        for (Stop s : stops) {
            Location l = new Location("foo");
            l.setLatitude(s.getStopLat());
            l.setLongitude(s.getStopLon());
            s.setDistance(location.distanceTo(l));
        }


        Collections.sort(stops);
        return stops.get(0);
    }
}
