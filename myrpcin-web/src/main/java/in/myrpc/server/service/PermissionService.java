/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Permission;
import in.myrpc.shared.model.Role;
import in.myrpc.shared.model.User;

/**
 * Methods for interacting with permissions objects
 *
 * @author kguthrie
 */
public interface PermissionService {

    Permission createNew(Account account, User user, Role type);

    Iterable<Permission> getForUser(User user);

}
