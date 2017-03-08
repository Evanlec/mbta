
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Trip implements Comparable<Trip> {

    @JsonProperty("trip_id")
    public String tripId;
    @JsonProperty("trip_name")
    public String tripName;
    @JsonProperty("trip_headsign")
    public String tripHeadsign;
    @JsonProperty("sch_arr_dt")
    public String schArrDt;
    @JsonProperty("sch_dep_dt")
    public String schDepDt;
    @JsonProperty("pre_dt")
    public String preDt;
    @JsonProperty("pre_away")
    public String preAway;
    @JsonProperty("vehicle")
    public Vehicle vehicle;

    public String getPreAwayFormatted() {
        Integer minutes = Integer.parseInt(preAway) / 60;
        Integer seconds = Integer.parseInt(preAway) % 60;
        if (minutes < 1 && seconds > 30) {
            return "Approaching";
        } else if (minutes < 1 && seconds <= 30) {
            return "Arriving";
        }
        return (minutes + "m" + seconds + "s");
    }

    // for sorting by closest train first
    @Override
    public int compareTo(Trip another) {
        Integer current = Integer.parseInt(this.preAway);
        Integer other = Integer.parseInt(another.preAway);

        return current.compareTo(other);
    }
}
