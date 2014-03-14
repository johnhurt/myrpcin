/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import in.myrpc.server.service.UserService;
import in.myrpc.shared.model.User;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author kguthrie
 */
@Path("/verify")
@Singleton
public class EmailVerificationServlet {

    private final UserService userService;

    @Inject
    public EmailVerificationServlet(UserService userService) {
        this.userService = userService;
    }

    @Path("/")
    @GET
    public Response verify(
            @QueryParam("code") String verificationCode) throws Exception {

        User user = userService.getByVerificationCode(verificationCode);

        if (user != null) {
            userService.verifyUser(user);
        }

        return Response.seeOther(new URI("http://myrpcin.appspot.com")).build();
    }

}
