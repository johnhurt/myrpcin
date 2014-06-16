/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * relay of an rpc command that can be sent to
 *
 * @author kguthrie
 */
public class RpcRelay {

    private final String method;
    private final Map<String, String> arguments;
    private final String originLocator;
    private final String requestId;

    public RpcRelay(
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, String> arguments,
            @JsonProperty("originLocator") String originLocator,
            @JsonProperty("requestId") String requestId) {
        this.method = method;
        this.arguments = arguments;
        this.originLocator = originLocator;
        this.requestId = requestId;
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
    public String getOriginLocator() {
        return originLocator;
    }

    /**
     * @return the responseId
     */
    public String getRequestId() {
        return requestId;
    }

}
