/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.gwt.dev.util.collect.Maps;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;
import java.util.Map;
import java.util.UUID;
import org.orgama.server.Ofy;

/**
 * implementation of the centerpoint service interface
 *
 * @author kguthrie
 */
public class CenterpointServiceImpl implements CenterpointService {

    private final LocatorService locatorService;

    @Inject
    public CenterpointServiceImpl(LocatorService locatorService) {
        Ofy.register(Centerpoint.class);
        this.locatorService = locatorService;
    }

    /**
     * Create a new centerpoint with the given aspects. A new uuid will be
     * generated as a key.
     *
     * @param account
     * @param name
     * @return
     */
    private Centerpoint createNew(Account account, String name) {

        String connectionCode = UUID.randomUUID().toString();

        Centerpoint result = new Centerpoint(account, name, connectionCode);

        Ofy.save().entity(result).now();
        return result;

    }

    /**
     * Create a centerpoint for the account. This centerpoint will have a
     * default name derived from the name of the account
     *
     * @param account
     * @return
     */
    @Override
    public Centerpoint createDefaultForAccount(Account account) {
        assert (account != null);
        String name = account.getName() + "'s First Centerpoint";

        return createNew(account, name);
    }

    /**
     * Get a list of all the centerpoints belonging to the given account
     *
     * @param account
     * @return
     */
    @Override
    public Iterable<Centerpoint> getCenterpointsForAccount(Account account) {
        assert (account != null);

        return Ofy.load().type(Centerpoint.class).ancestor(account).iterable();
    }

    @Override
    public Centerpoint getByKey(Key<Centerpoint> key) {
        return Ofy.load().now(key);
    }

    /**
     * Get the String that will allow endpoints to join to this centerpoint
     *
     * @param centerpoint
     * @return
     */
    @Override
    public String getJoinableLocator(Centerpoint centerpoint) {
        Map<String, String> headers = Maps.create(
                Centerpoint.CONNECTION_CODE, centerpoint.getConnectionCode());
        return locatorService.createLocator(centerpoint, headers);
    }

}
