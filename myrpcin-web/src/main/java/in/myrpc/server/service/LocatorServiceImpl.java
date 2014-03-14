/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Base64;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import in.myrpc.server.model.Locator;
import java.io.IOException;
import java.util.Map;

/**
 * implementation of the locator server interface
 *
 * @author kguthrie
 */
public class LocatorServiceImpl implements LocatorService {

    private final ObjectMapper mapper;

    @Inject
    public LocatorServiceImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Creates a locator string for the given entity and no headers
     *
     * @param <T>
     * @param entity
     * @return
     */
    @Override
    public <T> String createLocator(T entity) {
        return this.createLocatorFromKey(Key.create(entity));
    }

    /**
     * Creates a locator string for the given entity and the given headers
     *
     * @param <T>
     * @param entity
     * @param headers
     * @return
     */
    @Override
    public <T> String createLocator(T entity, Map<String, String> headers) {
        return this.createLocatorFromKey(Key.create(entity), headers);
    }

    public <T> String createLocatorFromKey(Key<T> key) {
        return this.createLocatorFromKey(key, null);
    }

    public <T> String createLocatorFromKey(
            Key<T> key, Map<String, String> headers) {
        assert (key != null);

        Locator result = new Locator(key, headers);
        try {
            return base62Encode(mapper.writeValueAsString(result));
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to serialize locator", ex);
        }
    }

    @Override
    public Locator openLocator(String locatorString) {

        if (locatorString == null) {
            return null;
        }

        String jsonLocator = base62Decode(locatorString);

        try {
            return mapper.readValue(jsonLocator, Locator.class);
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to deserialize locator", ex);
        }
    }

    /**
     * Encode the given string in base62
     *
     * @param toEncode
     * @return
     */
    private String base62Encode(String toEncode) {

        if (toEncode == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(toEncode.length() * 2);

        String base64Result = Base64.encodeBase64String(toEncode.getBytes());

        for (int i = 0; i < base64Result.length(); i++) {
            char c = base64Result.charAt(i);

            switch (c) {
                case 'i': {
                    result.append("ii");
                    break;
                }
                case '+': {
                    result.append("ip");
                    break;
                }
                case '=': {
                    result.append("ie");
                    break;
                }
                case '/':
                case '\\': {
                    result.append("is");
                    break;
                }
                case '\t':
                case ' ':
                case '\n': {
                    break;
                }
                default: {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    /**
     * Decode the given string from base 62
     *
     * @param toDecode
     * @return
     */
    private String base62Decode(String toDecode) {

        if (toDecode == null) {
            return null;
        }

        StringBuilder base64Result = new StringBuilder(toDecode.length() / 2);

        for (int i = 0; i < toDecode.length(); i++) {
            char c = toDecode.charAt(i);

            if (c == 'i') {
                c = toDecode.charAt(++i);
                switch (c) {
                    case 'i': {
                        base64Result.append('i');
                        break;
                    }
                    case 's': {
                        base64Result.append('/');
                        break;
                    }
                    case 'p': {
                        base64Result.append('+');
                        break;
                    }
                    case 'e': {
                        base64Result.append('=');
                        break;
                    }
                }
            }
            else {
                base64Result.append(c);
            }
        }

        return new String(Base64.decodeBase64(base64Result.toString()));
    }

}
