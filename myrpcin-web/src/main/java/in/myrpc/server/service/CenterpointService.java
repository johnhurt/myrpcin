/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.googlecode.objectify.Key;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;

/**
 * methods for interacting with centerpoints
 *
 * @author kguthrie
 */
public interface CenterpointService {

    Centerpoint getByKey(Key<Centerpoint> key);

    Centerpoint createDefaultForAccount(Account account);

    Iterable<Centerpoint> getCenterpointsForAccount(Account account);

    String getJoinableLocator(Centerpoint centerpoint);

}
