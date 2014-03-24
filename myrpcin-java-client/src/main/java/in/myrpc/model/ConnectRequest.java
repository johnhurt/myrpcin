/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a request for an endpoint to connect to its centerpoint
 *
 * @author kguthrie
 */
public class ConnectRequest {

    private final String endpointLocator;

    @JsonCreator
    public ConnectRequest(
            @JsonProperty("endpointLocator") String endpointLocator) {
        this.endpointLocator = endpointLocator;
    }

    /**
     * @return the endpointLocator
     */
    public String getEndpointLocator() {
        return endpointLocator;
    }

}
