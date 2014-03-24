/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.server.model.PooledChannel;

/**
 * Service for managing the pool of created channels
 *
 * @author kguthrie
 */
public interface PooledChannelService {

    PooledChannel getNewForEndpoint(String endpointLocator);

    PooledChannel get(String id);

    PooledChannel getByEndoint(String endpointLocator);

    void releaseById(String id);

    void deleteById(String id);

    void sendMessage(String endpointLocator, String message);
}
