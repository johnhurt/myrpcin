/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import in.myrpc.server.model.Locator;
import in.myrpc.shared.model.Centerpoint;
import in.myrpc.shared.model.Endpoint;
import org.orgama.server.Ofy;
import org.orgama.server.unique.except.UniqueFieldRestrictionException;

/**
 * implementation of the endpoint service interface
 *
 * @author kguthrie
 */
public class EndpointServiceImpl implements EndpointService {

    private final CenterpointService centerpointService;
    private final LocatorService locatorService;

    @Inject
    public EndpointServiceImpl(CenterpointService centerpointService,
            LocatorService locatorService) {
        this.centerpointService = centerpointService;
        this.locatorService = locatorService;
        Ofy.register(Endpoint.class);
    }

    /**
     * Provision an endpoint with the given name and
     *
     * @param endpointName
     * @param centerpointLocator
     * @return null if there is no centerpoint for the given id
     */
    @Override
    public String provisionEndpoint(String endpointName,
            String centerpointLocator) {

        Locator locator = locatorService.openLocator(centerpointLocator);

        String connectionCode = locator.getHeader(Centerpoint.CONNECTION_CODE);

        Centerpoint centerpoint
                = centerpointService.getByKey(locator.getKey());

        // If there is no centerpoint for the given id, then return null
        if (centerpoint == null) {
            return null;
        }

        // Make sure the connection code in the locator matches the connection
        // code in the centerpoint it points to
        if (!Objects.equal(connectionCode, centerpoint.getConnectionCode())) {
            throw new RuntimeException("Centerpoint locator is not valid");
        }

        Endpoint result;

        try {
            result = new Endpoint(centerpoint, endpointName);

            // Automatically accept endpoints for now
            result.setAccepted(true);

            Ofy.save().entity(result).now();

            return locatorService.createLocator(result);
        }
        catch (UniqueFieldRestrictionException ufex) {
            throw new RuntimeException(
                    "An endpoint with that name already exists "
                    + "in this centerpoint");
        }

    }
}
