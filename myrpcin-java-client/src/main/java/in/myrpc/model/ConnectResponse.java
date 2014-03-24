package in.myrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * storage class for the response from the centerpoint to a connecting endpoint
 *
 * @author kguthrie
 */
public class ConnectResponse {
    private final String channelToken;

    @JsonCreator
    public ConnectResponse(@JsonProperty("channelToken") String channelToken) {
        this.channelToken = channelToken;
    }

    /**
     * @return the channelToken
     */
    public String getChannelToken() {
        return channelToken;
    }

}
