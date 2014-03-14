/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.inject.Inject;
import in.myrpc.server.config.AppTstModule;
import in.myrpc.server.model.Locator;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;
import java.util.HashMap;
import java.util.Map;
import org.jukito.JukitoRunner;
import org.jukito.TestSingleton;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orgama.server.Ofy;
import org.orgama.server.config.OrgamaTestEnv;

/**
 *
 * @author kguthrie
 */
@RunWith(JukitoRunner.class)
public class LocatorServiceTest {

    public static class Module extends AppTstModule {

        @Override
        public void configureTest() {
            super.configureTest();
            bind(AccountService.class)
                    .to(AccountServiceImpl.class)
                    .in(TestSingleton.class);
            bind(PermissionService.class)
                    .to(PermissionServiceImpl.class)
                    .in(TestSingleton.class);
            bind(CenterpointService.class)
                    .to(CenterpointServiceImpl.class)
                    .in(TestSingleton.class);
            bind(EndpointService.class)
                    .to(EndpointServiceImpl.class)
                    .in(TestSingleton.class);
            bind(LocatorService.class)
                    .to(LocatorServiceImpl.class)
                    .in(TestSingleton.class);
        }

    }

    @Inject
    LocatorService locatorService;

    @Inject
    AccountService accountService;

    @Inject
    CenterpointService centerpointService;

    @Inject
    OrgamaTestEnv testEnv;

    @Test
    public void testCreatingLocator() {
        Account a = new Account("account");
        Ofy.save().entity(a).now();

        Centerpoint cp = new Centerpoint(a, "cp", "code");

        Ofy.save().entity(cp).now();

        String ls = locatorService.createLocator(cp);

        Locator l = locatorService.openLocator(ls);

        Centerpoint cp2 = (Centerpoint) Ofy.load().key(l.getKey()).now();

        Assert.assertNotNull(cp2);
        Assert.assertEquals(cp.getId(), cp2.getId());

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Centerpoint.CONNECTION_CODE, "BLAMMO");

        ls = locatorService.createLocator(cp, headers);

        l = locatorService.openLocator(ls);

        Assert.assertNotNull(l.getHeader(Centerpoint.CONNECTION_CODE));
        Assert.assertEquals(headers.get(Centerpoint.CONNECTION_CODE),
                l.getHeader(Centerpoint.CONNECTION_CODE));
    }

}
