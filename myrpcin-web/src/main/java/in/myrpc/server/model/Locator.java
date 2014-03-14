/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * representation of a class that can be used to serialize an entity key and a
 * set of headers pertaining to it
 *
 * @author kguthrie
 */
public class Locator<T> {

    @JsonIgnore
    private final Key<T> key;

    @JsonProperty("headers")
    private final Map<String, String> headers;

    @JsonCreator
    public Locator(@JsonProperty("keyString") String keyString,
            @JsonProperty("headers") Map<String, String> headers) {
        this(Key.create(keyString), headers);
    }

    public Locator(Key key, Map<String, String> headers) {
        this.key = key;
        this.headers = (headers == null)
                ? new HashMap<String, String>()
                : Maps.newHashMap(headers);

    }

    public Locator(Key<T> key) {
        this(key, null);
    }

    /**
     * @return the key
     */
    @JsonIgnore
    public Key<T> getKey() {
        return key;
    }

    /**
     * Get the web-safe string version of the key
     *
     * @return
     */
    public String getKeyString() {
        return key.getString();
    }

    /**
     * get the header value for th given name;
     *
     * @param name
     * @return the headers
     */
    @JsonIgnore
    public String getHeader(String name) {
        return headers.get(name);
    }

}
