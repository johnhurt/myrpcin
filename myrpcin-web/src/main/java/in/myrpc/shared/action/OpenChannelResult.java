/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.action;

import org.orgama.shared.action.AbstractResult;

/**
 *
 * @author kguthrie
 */
public class OpenChannelResult extends AbstractResult {

    private String token;

    public OpenChannelResult() {
    }

    public OpenChannelResult(String token) {
        this.token = token;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

}
