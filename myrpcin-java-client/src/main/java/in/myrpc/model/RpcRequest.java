package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Model class that encapsulates the content for a remote procedure call
 *
 * @author kguthrie
 */
public class RpcRequest {

    private final String requestId;
    private final String sourceLocator;
    private final String targetLocator;
    private final String method;
    private final Map<String, String> arguments;

    @JsonCreator
    public RpcRequest(
            @JsonProperty("requestId") String requestId,
            @JsonProperty("sourceLocator") String sourceLocator,
            @JsonProperty("targetLocator") String targetLocator,
            @JsonProperty("method") String method,
            @JsonProperty("arguments") Map<String, String> arguments) {
        this.requestId = requestId;
        this.method = method;
        this.arguments = arguments;
        this.sourceLocator = sourceLocator;
        this.targetLocator = targetLocator;
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
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }


}
