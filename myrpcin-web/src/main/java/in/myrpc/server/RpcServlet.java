/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import in.myrpc.model.Rpc;
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

    private final ObjectMapper mapper;

    @Inject
    public RpcServlet() {
        this.mapper = new ObjectMapper();
    }

    @POST
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String rpc(String postContent) throws IOException {

        Rpc call = mapper.readValue(postContent, Rpc.class);

        return "boyee";
    }

}
