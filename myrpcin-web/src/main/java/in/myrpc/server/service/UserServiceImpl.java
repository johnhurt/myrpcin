/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import in.myrpc.shared.model.User;
import java.util.UUID;
import org.orgama.server.Ofy;
import org.orgama.shared.auth.model.AuthUser;

/**
 * Implementation of the user service
 *
 * @author kguthrie
 */
public class UserServiceImpl implements UserService {

    @Inject
    public UserServiceImpl() {
        Ofy.register(User.class);
    }

    /**
     * Get the user entity for the given auth user
     *
     * @param authUser
     * @return
     */
    @Override
    public User getFor(AuthUser authUser) {
        assert (authUser != null);

        Key<AuthUser> parentKey
                = Key.create(AuthUser.class, authUser.getUserId());
        Key<User> key = Key.create(parentKey, User.class, authUser.getUserId());

        return Ofy.load().key(key).now();
    }

    /**
     * Create and store a user for the given authUser and verification code. The
     * user object is created as unverified initially, and the verification code
     * is used to verify it.
     *
     * @param authUser
     * @param verificationCode
     * @return
     */
    @Override
    public User create(AuthUser authUser, String verificationCode) {
        User result = new User(authUser);
        result.setFirstVisitDateTime(System.currentTimeMillis());
        result.setVerificationCode(verificationCode);

        Ofy.save().entity(result).now();

        return result;
    }

    /**
     * Generates a new random string to be used as a verification code for new
     * users
     *
     * @return
     */
    @Override
    public String createVerificationCode() {

        return UUID.randomUUID().toString();

    }

    @Override
    public User getByVerificationCode(String verificationCode) {
        for (User user : Ofy.load().type(User.class).filter(
                "verificationCode", verificationCode).iterable()) {
            return user;
        }

        return null;
    }

    /**
     * Mark the given user as verified
     *
     * @param user
     */
    @Override
    public void verifyUser(User user) {
        user.setVerificationCode(null);
        user.setVerifiedDateTime(System.currentTimeMillis());
        Ofy.save().entity(user).now();

    }

    @Override
    public boolean isUserVerified(User user) {
        return user != null && user.getVerifiedDateTime() != null;
    }

    @Override
    public boolean isThisFirstVerifiedVisit(User user) {
        return user != null && user.getFirstVerifierVisitDateTime() == null;
    }

    @Override
    public void indicateUsersFirstVerifiedVisit(User user) {
        user.setFirstVerifierVisitDateTime(System.currentTimeMillis());
        Ofy.save().entity(user).now();
    }

}
