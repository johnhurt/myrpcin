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
import org.orgama.server.Ofy;

/**
 * implmentation of the permission services interface
 *
 * @author kguthrie
 */
public class PermissionServiceImpl implements PermissionService {

    @Inject
    public PermissionServiceImpl() {
        Ofy.register(Permission.class);
    }

    @Override
    public Permission createNew(Account account, User user, Role type) {
        assert (user != null);
        assert (account != null);

        Permission result = new Permission(account, user.getId(), type);
        Ofy.save().entity(result);

        return result;
    }

    @Override
    public Iterable<Permission> getForUser(User user) {
        return Ofy.load()
                .type(Permission.class)
                .filter("userId", user.getId())
                .iterable();
    }

}
