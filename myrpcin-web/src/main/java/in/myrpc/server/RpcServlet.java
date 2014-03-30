/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;
import in.myrpc.model.RpcRelay;
import in.myrpc.model.RpcRequest;
import in.myrpc.model.RpcResponse;
import in.myrpc.model.RpcReturn;
import in.myrpc.model.RpcReturnRelay;
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
 * Restful endpoints for handling actual rpc calls.
 *
 * @author kguthrie
 */
@Path("/pc")
public class RpcServlet {

    private final EndpointService endpointService;
    private final PooledChannelService channelService;
    private final ObjectMapper mapper;

    @Inject
    public RpcServlet(EndpointService endpointService,
            PooledChannelService channelService) {
        this.mapper = new ObjectMapper();
        this.endpointService = endpointService;
        this.channelService = channelService;
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String rpc(String postContent) throws IOException {

        RpcRequest call = mapper.readValue(postContent, RpcRequest.class);
        RpcResponse result;

        String sourceLocator = call.getSourceLocator();
        String targetLocator = call.getTargetLocator();

        Endpoint source = endpointService.getByLocator(sourceLocator);
        Endpoint target = endpointService.getByLocator(targetLocator);

        if (source == null || target == null
                || !source.getCenterpointRef().equivalent(
                        target.getCenterpointRef())) {
            return null;
        }

        PooledChannel channel = channelService.getByEndoint(targetLocator);

        if (channel == null) {
            return null;
        }

        RpcRelay rpc = new RpcRelay(call.getMethod(), call.getArguments(),
                sourceLocator, call.getRequestId());

        ChannelServiceFactory.getChannelService().sendMessage(
                new ChannelMessage(Long.toHexString(channel.getId()),
                        mapper.writeValueAsString(rpc)));

        result = new RpcResponse(rpc.getRequestId());

        return mapper.writeValueAsString(result);
    }

    @POST
    @Path("/return/")
    @Produces(MediaType.TEXT_PLAIN)
    public String rpcReturn(String postContent) throws IOException {

        RpcReturn returnCall = mapper.readValue(postContent, RpcReturn.class);

        String sourceLocator = returnCall.getSourceLocator();
        String targetLocator = returnCall.getTargetLocator();

        Endpoint source = endpointService.getByLocator(sourceLocator);
        Endpoint target = endpointService.getByLocator(targetLocator);

        if (source == null || target == null
                || !source.getCenterpointRef().equivalent(
                        target.getCenterpointRef())) {
            return null;
        }

        PooledChannel channel = channelService.getByEndoint(sourceLocator);

        if (channel == null) {
            return null;
        }

        RpcReturnRelay rpcReturn = new RpcReturnRelay(returnCall.getRequestId(),
                returnCall.getReturnValue());

        ChannelServiceFactory.getChannelService().sendMessage(
                new ChannelMessage(Long.toHexString(channel.getId()),
                        mapper.writeValueAsString(rpcReturn)));

        return null;
    }
}
