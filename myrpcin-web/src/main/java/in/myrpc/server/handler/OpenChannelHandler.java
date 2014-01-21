/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.handler;

import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.server.ExecutionContext;
import in.myrpc.server.service.DeviceService;
import in.myrpc.shared.action.OpenChannel;
import in.myrpc.shared.action.OpenChannelResult;
import in.myrpc.shared.model.Device;
import org.orgama.server.handler.AbstractHandler;

/**
 *
 * @author kguthrie
 */
public class OpenChannelHandler
        extends AbstractHandler<OpenChannel, OpenChannelResult> {

    private final DeviceService deviceService;

    @Inject
    public OpenChannelHandler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public OpenChannelResult execImpl(OpenChannel a, ExecutionContext ec) {

        String username = a.getUserName();

        Device d = new Device(username);

        deviceService.save(d);

        return new OpenChannelResult(
                ChannelServiceFactory.getChannelService().createChannel(
                        a.getUserName()));
    }

    @Override
    public void undoImpl(OpenChannel a, OpenChannelResult r, ExecutionContext ec) {

    }

    @Override
    public Class<OpenChannel> getActionType() {
        return OpenChannel.class;
    }

}
