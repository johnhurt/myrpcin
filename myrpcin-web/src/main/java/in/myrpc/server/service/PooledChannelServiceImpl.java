/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.myrpc.server.service;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import in.myrpc.server.model.PooledChannel;
import org.orgama.server.Ofy;

/**
 * Service for managing the pool of unused channels that have been created but
 * are not in use
 *
 * @author kguthrie
 */
public class PooledChannelServiceImpl implements PooledChannelService {

    @Inject
    public PooledChannelServiceImpl() {
        Ofy.register(PooledChannel.class);
    }

    @Override
    public PooledChannel getNewForEndpoint(final String endpointLocator) {

        PooledChannel result = null;
        long now = System.currentTimeMillis();

        // Get the list of keys for potential channels
        Iterable<Key<PooledChannel>> unusedChannels = Ofy.load()
                .type(PooledChannel.class)
                .filter("endpointLocator", null)
                .filter("expirationDate >", now)
                .keys()
                .iterable();

        // Iterate through the list of potential channel keys until one can
        // be transactionally acquired for the endpoint
        for (final Key<PooledChannel> channelKey : unusedChannels) {

            result = Ofy.transact(new Work<PooledChannel>() {

                @Override
                public PooledChannel run() {
                    PooledChannel result
                            = Ofy.load()
                            .key(channelKey)
                            .now();

                    // if the channel's endpoint has been set, someone else
                    // took this channel before we could
                    if (result.getEndpointLocator() != null) {
                        return null;
                    }

                    result.setEndpointLocator(endpointLocator);

                    Ofy.save().entity(result).now();

                    return result;
                }
            });

            if (result != null) {
                break;
            }
        }


        // If the result is null, we need to create a new, real channel, and
        // save a represntation of if in the pool
        if (result == null) {
            result = new PooledChannel();
            result.setEndpointLocator("somethingNotNull");
            Ofy.save().entity(result).now(); // To get a unique id
            String token = ChannelServiceFactory.getChannelService()
                    .createChannel(Long.toHexString(result.getId()));
            long expireDate = System.currentTimeMillis()
                    + (long) (1.99 * 60L * 60L * 1000L);
            result.setEndpointLocator(endpointLocator);
            result.setExpirationDate(expireDate);
            result.setToken(token);

            Ofy.save().entity(result).now();
        }

        return result;
    }

    @Override
    public PooledChannel get(String id) {
        return get(Long.parseLong(id, 16));
    }


    public PooledChannel get(long id) {
        return Ofy.load().type(PooledChannel.class).id(id).now();
    }

    @Override
    public PooledChannel getByEndoint(String endpointId) {
        Iterable<PooledChannel> resultIterable = Ofy.load()
                .type(PooledChannel.class)
                .filter("endpointLocator", endpointId).iterable();

        // Start iterating through the result of the query, and return the first
        // result
        for (PooledChannel result : resultIterable) {

            // If the resulting entity is expired, then delete it and return
            // null
            if (result.getExpirationDate() < System.currentTimeMillis()) {
                deleteById(result.getId());
                return null;
            }
            return result;
        }

        // If there are no results from the query, there is no pooled channel
        // assigned to the endpoint id given
        return null;
    }

    @Override
    public void releaseById(String id) {
        releaseById(Long.parseLong(id, 16));
    }

    @Override
    public void releaseById(final long id) {
        Ofy.transact(new VoidWork() {

            @Override
            public void vrun() {

                //Get the channel being reslease
                PooledChannel channel = get(id);

                // Probably wont happend, but whatever
                if (channel == null) {
                    return;
                }

                //If the channel is expired, then delete it asynchronously
                if (channel.getExpirationDate() < System.currentTimeMillis()) {
                    deleteById(id);
                }
                else {
                    channel.setEndpointLocator(null);
                    Ofy.save().entity(channel).now();
                }
            }
        });
    }

    @Override
    public void deleteById(long id) {
        Ofy.delete().key(Key.create(PooledChannel.class, id));
    }

    @Override
    public void sendMessage(String endpointId, String message) {
        PooledChannel channel = getByEndoint(endpointId);

        ChannelMessage msg = new ChannelMessage(
                Long.toHexString(channel.getId()), message);

        ChannelServiceFactory.getChannelService().sendMessage(msg);
    }

    @Override
    public void placeChannelOnHoldUntilDisconnect(final long id,
            final String expectedEndpoingLocator) {
        assert(expectedEndpoingLocator != null);

        Ofy.transact(new VoidWork() {

            @Override
            public void vrun() {

                //Get the channel being resleases
                PooledChannel channel = get(id);

                // Probably wont happend, but whatever
                if (channel == null) {
                    return;
                }

                // endpoint locator has changed since this channel was put on
                // hold, so we shouldn't mess with it
                if (!expectedEndpoingLocator.equals(
                        channel.getEndpointLocator())) {
                    return;
                }

                //If the channel is expired, then delete it asynchronously
                if (channel.getExpirationDate() < System.currentTimeMillis()) {
                    deleteById(id);
                }
                else {
                    channel.setEndpointLocator("__OUT_OF_SERVICE__");
                    Ofy.save().entity(channel).now();
                }
            }
        });
    }

}
