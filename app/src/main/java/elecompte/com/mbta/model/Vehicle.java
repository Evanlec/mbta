
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Vehicle {

    @JsonProperty("vehicle_id")
    public String vehicleId;
    @JsonProperty("vehicle_lat")
    public String vehicleLat;
    @JsonProperty("vehicle_lon")
    public String vehicleLon;
    @JsonProperty("vehicle_bearing")
    public String vehicleBearing;
    @JsonProperty("vehicle_speed")
    public String vehicleSpeed;
    @JsonProperty("vehicle_timestamp")
    public String vehicleTimestamp;

}
