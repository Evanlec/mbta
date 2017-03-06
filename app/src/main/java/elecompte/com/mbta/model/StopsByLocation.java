
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class StopsByLocation {

    @JsonProperty("stop")
    public List<Stop> stops = new ArrayList<>();

}
