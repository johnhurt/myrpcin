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
public class RpcReturnRelay {

    private final String requestId;
    private final String returnValue;

    @JsonCreator
    public RpcReturnRelay(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("returnValue") String returnValue) {
        this.requestId = requestId;
        this.returnValue = returnValue;
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the returnValue
     */
    public String getReturnValue() {
        return returnValue;
    }

}
