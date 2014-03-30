/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.inject.Inject;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Permission;
import in.myrpc.shared.model.Role;
import in.myrpc.shared.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import org.orgama.server.Ofy;
import org.orgama.shared.auth.model.AuthUser;

/**
 * implementation of the account service interface
 *
 * @author kguthrie
 */
public class AccountServiceImpl implements AccountService {

    private final PermissionService permissionService;

    @Inject
    public AccountServiceImpl(PermissionService permissionService) {
        this.permissionService = permissionService;
        Ofy.register(Account.class);
    }

    @Override
    public Account createDefaultAccountForUser(User user) {
        assert (user != null);
        AuthUser authUser = user.getAuthUserRef().get();

        assert (authUser != null);
        String emailAddress = authUser.getOriginalEmailAddress();

        assert (emailAddress != null);
        String nickname = emailAddress.split("@", 2)[0];
        nickname = Character.toUpperCase(nickname.charAt(0))
                + nickname.substring(1);

        Account result = new Account(nickname + "'s Account");
        Ofy.save().entity(result).now();

        permissionService.createNew(
                result, user, Role.owner);

        return result;
    }

    @Override
    public HashMap<Role, ArrayList<Account>> getAccountsForUser(
            User user) {

        HashMap<Role, ArrayList<Account>> result
                = new HashMap<Role, ArrayList<Account>>();

        for (Permission permission : permissionService.getForUser(user)) {
            ArrayList<Account> accounts = result.get(
                    permission.getRole());

            if (accounts == null) {
                accounts = new ArrayList<Account>();
                result.put(permission.getRole(), accounts);
            }

            accounts.add(permission.getAccountRef().get());
        }

        return result;
    }

}
