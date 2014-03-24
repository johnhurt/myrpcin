
package in.myrpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import in.myrpc.model.ConnectRequest;
import in.myrpc.model.ConnectResponse;
import in.myrpc.server.model.PooledChannel;
import in.myrpc.server.service.EndpointService;
import in.myrpc.server.service.PooledChannelService;
import in.myrpc.shared.model.Endpoint;
import java.io.IOException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Servlet for handing the connection of an endpoint to its centerpoint
 *
 * @author kguthrie
 */
@Path("/connect")
public class EndpointConnectionServlet {

    private final ObjectMapper mapper;
    private final EndpointService endpointService;
    private final PooledChannelService channelService;

    @Inject
    public EndpointConnectionServlet(EndpointService endpointService,
            PooledChannelService channelService) {
        mapper = new ObjectMapper();
        this.channelService = channelService;
        this.endpointService = endpointService;
    }

    @Path("/")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String connect(String postContent) throws IOException {
        ConnectRequest connectRequest = mapper.readValue(postContent,
                ConnectRequest.class);

        String endpointLocator = connectRequest.getEndpointLocator();

        if (endpointLocator == null) {
            return null;
        }

        Endpoint endpoint = endpointService.getByLocator(
                connectRequest.getEndpointLocator());

        if (endpoint == null || !endpoint.isAccepted()) {
            return null;
        }

        PooledChannel channel = channelService.getByEndoint(endpointLocator);

        if (channel == null) {
            channel = channelService.getNewForEndpoint(endpointLocator);
        }

        ConnectResponse result = new ConnectResponse(channel.getToken());

        return mapper.writeValueAsString(result);
    }

}
