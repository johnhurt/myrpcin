package in.myrpc.receiver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author kguthrie
 */
public class AppEngineChannelConnectPayload {

    private final String cn;
    private final String lpu;
    private final String ppu;
    private final String tp;

    public AppEngineChannelConnectPayload(
            @JsonProperty("cn") String cn,
            @JsonProperty("lpu") String lpu,
            @JsonProperty("ppu") String ppu,
            @JsonProperty("tp") String tp) {
        this.cn = cn;
        this.lpu = lpu;
        this.ppu = ppu;
        this.tp = tp;
    }

    /**
     * @return the cn
     */
    public String getCn() {
        return cn;
    }

    /**
     * @return the lpu
     */
    public String getLpu() {
        return lpu;
    }

    /**
     * @return the ppu
     */
    public String getPpu() {
        return ppu;
    }

    /**
     * @return the tp
     */
    public String getTp() {
        return tp;
    }



}
