
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown=true)
public class Stop implements Comparable<Stop> {

    @JsonProperty("stop_id")
    public String stopId;
    @JsonProperty("stop_name")
    public String stopName;
    @JsonProperty("parent_station")
    public String parentStation;
    @JsonProperty("parent_station_name")
    public String parentStationName;
    @JsonProperty("stop_lat")
    public String stopLat;
    @JsonProperty("stop_lon")
    public String stopLon;
    @JsonIgnore
    private Float distance = 0.0f;

    public Double getStopLat() {
        return Double.parseDouble(stopLat);
    }
    public Double getStopLon() {
        return Double.parseDouble(stopLon);
    }

    public Float getDistance() {
        return this.distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public String getParentStation() { return parentStation; }

    public String getParentStationName() { return parentStationName; }

    @Override
    public int compareTo(Stop other) {
        return this.getDistance().compareTo(other.getDistance());
    }

}

