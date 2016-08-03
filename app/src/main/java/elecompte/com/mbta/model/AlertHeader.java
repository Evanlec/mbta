
package elecompte.com.mbta.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class AlertHeader {

    @JsonProperty("alert_id")
    public Integer alertId;
    @JsonProperty("header_text")
    public String headerText;
    @JsonProperty("effect_name")
    public String effectName;

}
