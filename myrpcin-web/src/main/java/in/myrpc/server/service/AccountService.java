/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Role;
import in.myrpc.shared.model.User;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Methods for interacting with the account system
 *
 * @author kguthrie
 */
public interface AccountService {

    Account createDefaultAccountForUser(User user);

    EnumMap<Role, ArrayList<Account>> getAccountsForUser(User user);

}
