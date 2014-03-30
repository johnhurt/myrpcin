/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.action;

import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;
import in.myrpc.shared.model.Role;
import in.myrpc.shared.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import org.orgama.shared.action.AbstractResult;

/**
 * Result from a call to the get user info action
 *
 * @author kguthrie
 */
public class GetUserInfoResult extends AbstractResult {

    // This is a kluge because gwt won't make the enum serializable unless it's
    // explicitely in an object that's transfered.  Being in the enum map
    // doesn't count :P
    private Role wtf;

    private User user;
    private HashMap<Role, ArrayList<Account>> accounts;
    private HashMap<String, Centerpoint> centerpoints;

    public GetUserInfoResult() {
    }

    public GetUserInfoResult(User user,
            HashMap<Role, ArrayList<Account>> accounts,
            HashMap<String, Centerpoint> centerpoints) {
        this.user = user;
        this.accounts = accounts;
        this.centerpoints = centerpoints;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the accounts
     */
    public HashMap<Role, ArrayList<Account>> getAccounts() {
        return accounts;
    }

    /**
     * @return the centerpoints
     */
    public HashMap<String, Centerpoint> getCenterpoints() {
        return centerpoints;
    }

}
