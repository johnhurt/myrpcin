/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.model;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import java.io.Serializable;

/**
 * represents the hub for connecting a group of endpoints
 *
 * @author kguthrie
 */
@Entity
public class Centerpoint implements Serializable {

    public static final transient String CONNECTION_CODE = "connectionCode";

    @Parent
    @Load
    private Ref<Account> account;

    @Id
    private Long id;

    private String name;

    @GwtTransient
    private String connectionCode;

    public Centerpoint() {
    }

    public Centerpoint(Account account, String name, String connectionCode) {
        this.account = Ref.create(account);
        this.name = name;
        this.connectionCode = connectionCode;
    }

    /**
     * @return the account
     */
    public Ref<Account> getAccountRef() {
        return account;
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

    /**
     * @return the connectionCode
     */
    public String getConnectionCode() {
        return connectionCode;
    }

    /**
     * @param connectionCode the connectionCode to set
     */
    public void setConnectionCode(String connectionCode) {
        this.connectionCode = connectionCode;
    }

}
