/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.myrpc.server;

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;
import in.myrpc.server.service.PooledChannelService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * servlet for handling the appengine calls to indicate the connection or
 * disconnection or a client from a channel
 *
 * @author kguthrie
 */
public class ChannelPresenceServlet extends HttpServlet {

    private final PooledChannelService channelService;

    @Inject
    public ChannelPresenceServlet(PooledChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ChannelService channelService
                = ChannelServiceFactory.getChannelService();
        ChannelPresence presence = channelService.parsePresence(req);

        if (presence.isConnected()) {
            onConnect(presence.clientId());
        }
        else {
            onDisconnect(presence.clientId());
        }
    }

    /**
     * Called when the given channel is opened by a client
     *
     * @param channelId
     */
    private void onConnect(String channelId) {
        //this is where some audit logic should go to ensure that a channel is
        //not opened twice in a row before closing
    }

    /**
     * Called when the given channel is closed by the client
     *
     * @param channelId
     */
    private void onDisconnect(String channelId) {
        channelService.releaseById(channelId);
    }

}
