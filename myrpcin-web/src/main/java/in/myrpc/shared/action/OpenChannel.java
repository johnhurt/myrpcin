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
public class OpenChannel extends AbstractAction<OpenChannelResult> {

    private String userName;

    public OpenChannel() {
    }

    public OpenChannel(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

}
