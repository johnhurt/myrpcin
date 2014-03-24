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
public class RpcResponse {

    private final String requestId;

    @JsonCreator
    public RpcResponse(@JsonProperty("requestId") String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

}
