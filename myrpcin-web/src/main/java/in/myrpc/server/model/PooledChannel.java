/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * representation of a channel that can be stored in the datastore to indicate
 * its availability in the pool
 *
 * @author kguthrie
 */
@Entity
public final class PooledChannel {

    @Id
    private Long id;
    private String token;

    @Index
    private String endpointId;

    @Index
    private long expirationDate;

    public PooledChannel() {
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the endpointId
     */
    public String getEndpointId() {
        return endpointId;
    }

    /**
     * @param endpointId the endpointId to set
     */
    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    /**
     * @return the expirationDate
     */
    public long getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @param expirationDate the expirationDate to set
     */
    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

}
