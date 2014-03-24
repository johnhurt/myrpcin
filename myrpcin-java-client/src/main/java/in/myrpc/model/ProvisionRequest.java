package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * stores the information about a request for provisioning
 *
 * @author kguthrie
 */
public class ProvisionRequest {

    private final String endpointName;
    private final String centerpointLocator;

    @JsonCreator
    public ProvisionRequest(
            @JsonProperty("endpointName") String endpointName,
            @JsonProperty("centerpointLocator") String centerpointLocator) {
        this.endpointName = endpointName;
        this.centerpointLocator = centerpointLocator;
    }

    /**
     * @return the endpointName
     */
    public String getEndpointName() {
        return endpointName;
    }

    /**
     * @return the centerpointLocator
     */
    public String getCenterpointLocator() {
        return centerpointLocator;
    }

}
