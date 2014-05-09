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
    private final long secondsUntilExpire;

    @JsonCreator
    public ConnectResponse(
            @JsonProperty("channelToken") String channelToken,
            @JsonProperty("secondsUntilExpire") long secondsUntilExpire) {
        this.channelToken = channelToken;
        this.secondsUntilExpire = secondsUntilExpire;
    }

    /**
     * @return the channelToken
     */
    public String getChannelToken() {
        return channelToken;
    }

    /**
     * @return the secondsUntilExpire
     */
    public long getSecondsUntilExpire() {
        return secondsUntilExpire;
    }

}
