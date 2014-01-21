/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.Device;

/**
 *
 * @author kguthrie
 */
public interface DeviceService {

    void save(Device device);

    Device get(String username);

}
