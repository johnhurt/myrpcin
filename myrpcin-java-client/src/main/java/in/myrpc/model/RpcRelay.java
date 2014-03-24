/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * relay of an rpc command that can be sent to
 *
 * @author kguthrie
 */
public class RpcRelay {

    private final String method;
    private final Map<String, String> arguments;
    private final String returnEndpoint;
    private final String responseId;

    public RpcRelay(
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, String> arguments,
            @JsonProperty("returnChannel") String returnChannel,
            @JsonProperty("responseId") String responseId) {
        this.method = method;
        this.arguments = ImmutableMap.copyOf(arguments);
        this.returnEndpoint = returnChannel;
        this.responseId = responseId;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the arguments
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * @return the returnEndpoint
     */
    public String getReturnChannel() {
        return returnEndpoint;
    }

    /**
     * @return the responseId
     */
    public String getResponseId() {
        return responseId;
    }

}
