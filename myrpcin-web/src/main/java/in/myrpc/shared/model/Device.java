/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 *
 * @author kguthrie
 */
@Entity
public class Device {

    @Id
    private String username;

    public Device() {
    }

    public Device(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
