package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulation of the information needed to return the result of an rpc call
 *
 * @author kguthrie
 */
public class RpcReturn {

    private final String requestId;
    private final String returnValue;
    private final String sourceLocator;
    private final String targetLocator;

    @JsonCreator
    public RpcReturn(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("returnValue") String returnValue,
            @JsonProperty("sourceLocator") String sourceLocator,
            @JsonProperty("targetLocator") String targetLocator) {
        this.requestId = requestId;
        this.returnValue = returnValue;
        this.sourceLocator = sourceLocator;
        this.targetLocator = targetLocator;
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

    /**
     * @return the sourceLocator
     */
    public String getSourceLocator() {
        return sourceLocator;
    }

    /**
     * @return the targetLocator
     */
    public String getTargetLocator() {
        return targetLocator;
    }

}
