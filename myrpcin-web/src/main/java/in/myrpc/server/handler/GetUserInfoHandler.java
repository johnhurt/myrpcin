/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.handler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.dispatch.server.ExecutionContext;
import in.myrpc.server.service.AccountService;
import in.myrpc.server.service.CenterpointService;
import in.myrpc.server.service.EmailService;
import in.myrpc.server.service.UserService;
import in.myrpc.shared.action.GetUserInfo;
import in.myrpc.shared.action.GetUserInfoResult;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;
import in.myrpc.shared.model.Role;
import in.myrpc.shared.model.User;
import java.util.ArrayList;
import java.util.HashMap;
import org.orgama.server.auth.AuthUserService;
import org.orgama.server.auth.ICoreSessionService;
import org.orgama.server.handler.AbstractHandler;
import org.orgama.shared.Logger;
import org.orgama.shared.auth.model.AuthSession;
import org.orgama.shared.auth.model.AuthUser;

/**
 * Handler for the GetUserInfo action
 *
 * @author kguthrie
 */
public class GetUserInfoHandler
        extends AbstractHandler<GetUserInfo, GetUserInfoResult> {

    private final UserService userService;
    private final Provider<ICoreSessionService> authSessionServiceProvider;
    private final EmailService emailService;
    private final AuthUserService authUserService;
    private final AccountService accountService;
    private final CenterpointService centerpointService;

    @Inject
    public GetUserInfoHandler(UserService userService,
            Provider<ICoreSessionService> authSessionServiceProvider,
            EmailService emailService,
            AuthUserService authUserService,
            AccountService accountService,
            CenterpointService centerpointService) {
        this.authSessionServiceProvider = authSessionServiceProvider;
        this.authUserService = authUserService;
        this.userService = userService;
        this.emailService = emailService;
        this.accountService = accountService;
        this.centerpointService = centerpointService;
    }

    @Override
    public GetUserInfoResult execImpl(GetUserInfo a, ExecutionContext ec) {
        AuthSession authSession = authSessionServiceProvider.get().get();
        AuthUser authUser = authUserService.getUserById(
                authSession.getUserId());
        User user = userService.getFor(authUser);

        if (user != null) {

            // If the user is not verified, return an empty result indicating
            // we are still waiting on the user to click the link in their email
            if (!userService.isUserVerified(user)
                    && !"kevin.guthrie@gmail.com".equals(
                            authUser.getSanitizedEmailAddress())) {
                return new GetUserInfoResult();
            }

            // If this is the user's first visit, we have some special things
            // to take care of for them
            if (userService.isThisFirstVerifiedVisit(user)) {
                handleFirstVerifiedVisit(user);
            }

            // get the accounts the current user has permissions to
            HashMap<Role, ArrayList<Account>> accounts
                    = accountService.getAccountsForUser(user);

            HashMap<String, Centerpoint> centerpoints
                    = new HashMap<String, Centerpoint>();

            // Get all the centerpoints that the accounts contain
            for (ArrayList<Account> accountList : accounts.values()) {
                for (Account account : accountList) {
                    for (Centerpoint centerpoint
                            : centerpointService.getCenterpointsForAccount(
                                    account)) {
                        centerpoints.put(centerpointService.getJoinableLocator(
                                centerpoint), centerpoint);
                    }
                }
            }

            return new GetUserInfoResult(user, accounts, centerpoints);
        }


        String verificationCode = userService.createVerificationCode();
        user = userService.create(authUser, verificationCode);

        try {
            emailService.sendVerificationEmail(user);
        }
        catch (Exception ex) {
            Logger.error("Failed to send verification email", ex);
        }

        // Return empty result to indicate unverified user
        return new GetUserInfoResult();
    }

    /**
     * Handles the business logic for a verified user visiting for the first
     * time
     *
     * @param user
     */
    private void handleFirstVerifiedVisit(User user) {
        Account account = accountService.createDefaultAccountForUser(user);
        centerpointService.createDefaultForAccount(account);
        userService.indicateUsersFirstVerifiedVisit(user);
    }

    @Override
    public void undoImpl(GetUserInfo a, GetUserInfoResult r, ExecutionContext ec) {
        // Nothing to do here
    }

    @Override
    public Class<GetUserInfo> getActionType() {
        return GetUserInfo.class;
    }

}
