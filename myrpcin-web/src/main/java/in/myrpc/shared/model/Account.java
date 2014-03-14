/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import java.io.Serializable;

/**
 * Linkage object for associating centerpoints, user permissions, and payment
 * information
 *
 * @author kguthrie
 */
@Entity
public class Account implements Serializable {

    @Id
    private Long id;

    private String name;

    public Account() {
    }

    public Account(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
