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
import com.googlecode.objectify.annotation.Parent;
import java.io.Serializable;
import org.orgama.server.annotation.Unique;
import org.orgama.shared.unique.HasIdAndUniqueFields;

/**
 * represents the device or application receiving the rpc calls.
 *
 * @author kguthrie
 */
@Entity
public class Endpoint extends HasIdAndUniqueFields<Long>
        implements Serializable {

    @Parent
    private Ref<Centerpoint> centerpointRef;

    @Id
    private Long id;

    @Unique
    @Index
    private String name;

    private boolean accepted;

    public Endpoint() {
    }

    public Endpoint(Centerpoint centerpoint, String name) {
        this.centerpointRef = Ref.create(centerpoint);
        this.name = name;
        this.accepted = false;
    }

    /**
     * @return the centerpointRef
     */
    public Ref<Centerpoint> getCenterpointRef() {
        return centerpointRef;
    }

    /**
     * @return the id
     */
    @Override
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
     * @return the accepted
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * @param accepted the accepted to set
     */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }


}
