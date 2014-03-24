/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import in.myrpc.model.ProvisionRequest;
import in.myrpc.model.ProvisionResponse;
import in.myrpc.server.service.EndpointService;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Restful methods for provisioning endpoints within a centerpoint
 *
 * @author kguthrie
 */
@Path("/provision")
public class EndpointProvisioningServlet {

    private final EndpointService endpointService;
    private final ObjectMapper mapper;

    @Inject
    public EndpointProvisioningServlet(EndpointService endpointService) {
        this.endpointService = endpointService;
        this.mapper = new ObjectMapper();
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String provision(String postContent) throws Exception {

        ProvisionRequest request = mapper.readValue(postContent,
                ProvisionRequest.class);

        String result = endpointService.provisionEndpoint(
                request.getEndpointName(),
                request.getCenterpointLocator());

        return mapper.writeValueAsString(new ProvisionResponse(result));
    }

}
