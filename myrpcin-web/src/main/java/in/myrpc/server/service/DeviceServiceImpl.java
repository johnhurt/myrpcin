/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import com.google.inject.Inject;
import in.myrpc.shared.model.Device;
import org.orgama.server.Ofy;

/**
 *
 * @author kguthrie
 */
public class DeviceServiceImpl implements DeviceService {

    @Inject
    public DeviceServiceImpl() {
        Ofy.register(Device.class);
    }

    @Override
    public void save(Device device) {
        Ofy.save().entity(device).now();
    }

    @Override
    public Device get(String username) {
        return Ofy.load().type(Device.class).id(username).get();
    }

}
