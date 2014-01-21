/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.action;

import org.orgama.shared.action.AbstractAction;

/**
 *
 * @author kguthrie
 */
public class MessageDevice extends AbstractAction<MessageDeviceResult> {

    private String message;
    private String targetId;

    public MessageDevice() {
    }

    public MessageDevice(String message, String targetId) {
        this.message = message;
        this.targetId = targetId;
    }

    public String getMessage() {
        return message;
    }

    public String getTargetId() {
        return targetId;
    }

}
