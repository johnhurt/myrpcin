/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author kguthrie
 */
public class ProvisionResponse {

    private final String endpointLocator;

    @JsonCreator
    public ProvisionResponse(
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
