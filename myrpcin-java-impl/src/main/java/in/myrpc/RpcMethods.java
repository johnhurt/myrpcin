/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc;

import in.myrpc.annotation.RpcParam;

/**
 * Methods that can be called remotely via myRpc.in
 *
 * @author kguthrie
 */
public class RpcMethods {

    public String onMessage(@RpcParam("message") String message) {
        System.out.println("Received message: " + message);
        return System.currentTimeMillis() + "";
    }

}
