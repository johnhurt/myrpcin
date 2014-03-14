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
import com.googlecode.objectify.condition.IfNotNull;
import java.io.Serializable;
import org.orgama.shared.auth.model.AuthUser;

/**
 * Represents a user of the myrpc.in app
 *
 * @author kguthrie
 */
@Entity
public class User implements Serializable {

    @Parent
    @Load
    private Ref<AuthUser> authUserRef;

    @Id
    private Long id;

    private Long verifiedDateTime;
    private Long firstVisitDateTime;
    private Long firstVerifierVisitDateTime;

    @Index(IfNotNull.class)
    private String verificationCode;

    public User() {
    }

    /**
     * Create a user from its parent orgama auth user
     *
     * @param parent
     */
    public User(AuthUser parent) {
        assert (parent != null);
        id = parent.getUserId();
        authUserRef = Ref.create(parent);
    }

    /**
     * @return the authUser
     */
    public Ref<AuthUser> getAuthUserRef() {
        return authUserRef;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the verificationCode
     */
    public String getVerificationCode() {
        return verificationCode;
    }

    /**
     * @param verificationCode the verificationCode to set
     */
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    /**
     * @return the verifiedDateTime
     */
    public Long getVerifiedDateTime() {
        return verifiedDateTime;
    }

    /**
     * @return the firstVisitDateTime
     */
    public Long getFirstVisitDateTime() {
        return firstVisitDateTime;
    }

    /**
     * @return the firstVerifierVisitDateTime
     */
    public Long getFirstVerifierVisitDateTime() {
        return firstVerifierVisitDateTime;
    }

    /**
     * @param verifiedDateTime the verifiedDateTime to set
     */
    public void setVerifiedDateTime(Long verifiedDateTime) {
        this.verifiedDateTime = verifiedDateTime;
    }

    /**
     * @param firstVisitDateTime the firstVisitDateTime to set
     */
    public void setFirstVisitDateTime(Long firstVisitDateTime) {
        this.firstVisitDateTime = firstVisitDateTime;
    }

    /**
     * @param firstVerifierVisitDateTime the firstVerifierVisitDateTime to set
     */
    public void setFirstVerifierVisitDateTime(Long firstVerifierVisitDateTime) {
        this.firstVerifierVisitDateTime = firstVerifierVisitDateTime;
    }

}
