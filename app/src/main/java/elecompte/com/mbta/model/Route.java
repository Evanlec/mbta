
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class Route {

    @JsonProperty("route_id")
    public String routeId;
    @JsonProperty("route_name")
    public String routeName;
    @JsonProperty("direction")
    public List<Direction> direction = new ArrayList<>();

    public Direction getDirectionByDirectionId(String id) {

        for (Direction d : direction) {
            if (d.directionId.equals(id)) {
                return d;
            }
        }
        return null;
    }

}
