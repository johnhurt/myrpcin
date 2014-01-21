/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.handler;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.server.ExecutionContext;
import in.myrpc.server.service.DeviceService;
import in.myrpc.shared.action.MessageDevice;
import in.myrpc.shared.action.MessageDeviceResult;
import org.orgama.server.handler.AbstractHandler;

/**
 *
 * @author kguthrie
 */
public class MessageDeviceHandler
        extends AbstractHandler<MessageDevice, MessageDeviceResult> {

    private DeviceService deviceService;

    @Inject
    public MessageDeviceHandler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public MessageDeviceResult execImpl(MessageDevice a, ExecutionContext ec) {

        String message = a.getMessage();
        String userId = a.getTargetId();

        ChannelServiceFactory.getChannelService().sendMessage(
                new ChannelMessage(userId, message));

        return new MessageDeviceResult();
    }

    @Override
    public void undoImpl(MessageDevice a, MessageDeviceResult r, ExecutionContext ec) {

    }

    @Override
    public Class<MessageDevice> getActionType() {
        return MessageDevice.class;
    }

}
