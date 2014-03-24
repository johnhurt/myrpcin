/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.Endpoint;

/**
 * methods for interacting with endpoints
 *
 * @author kguthrie
 */
public interface EndpointService {

    String provisionEndpoint(String endpointName, String centerpointLocator);

    Endpoint getByLocator(String locator);

}
