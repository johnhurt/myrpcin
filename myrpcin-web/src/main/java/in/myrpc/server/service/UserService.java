/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.User;
import org.orgama.shared.auth.model.AuthUser;

/**
 * Service for interacting the with the users of myrpc
 *
 * @author kguthrie
 */
public interface UserService {

    User getFor(AuthUser authUser);

    User getByVerificationCode(String verificationCode);

    User create(AuthUser authUser, String verificationCode);

    String createVerificationCode();

    void verifyUser(User user);

    boolean isUserVerified(User user);

    boolean isThisFirstVerifiedVisit(User user);

    void indicateUsersFirstVerifiedVisit(User user);
}
