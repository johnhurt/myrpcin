/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.shared.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import java.io.Serializable;

/**
 * Linkage class between users and accountRefs
 *
 * @author kguthrie
 */
@Entity
public class Permission implements Serializable {

    @Parent
    @Load
    private Ref<Account> accountRef;

    @Id
    private Long id;

    @Index
    private long userId;

    private Role role;

    public Permission() {
    }

    public Permission(Account account, Long userId,
            Role role) {
        this.accountRef = Ref.create(account);
        this.userId = userId;
        this.role = role;
    }


    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the userId
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @return the accountRef
     */
    public Ref<Account> getAccountRef() {
        return accountRef;
    }

    /**
     * @return the permissionType
     */
    public Role getRole() {
        return role;
    }

}
