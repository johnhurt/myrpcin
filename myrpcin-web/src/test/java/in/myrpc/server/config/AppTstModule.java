/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.config;

import org.jukito.TestModule;
import org.orgama.server.config.OrgamaTestEnvModule;

/**
 *
 * @author kguthrie
 */
public class AppTstModule extends TestModule {

    @Override
    protected void configureTest() {
        install(new OrgamaTestEnvModule(
                getEmailAddress(),
                getAuthServiceName(),
                isAuthenticated(),
                isAdmin()));
    }

    /**
     * get the email address to use within the auth system
     */
    protected String getEmailAddress() {
        return "test@unit.com";
    }

    /**
     * get whether the user is authenticated in the test environment
     *
     * @return
     */
    protected boolean isAuthenticated() {
        return false;
    }

    /**
     * returns whether the given user is authenticated as an
     *
     * @return
     */
    protected boolean isAdmin() {
        return false;
    }

    /**
     * get the name of the auth service that the user is authenticated with
     */
    protected String getAuthServiceName() {
        return null;
    }

}
