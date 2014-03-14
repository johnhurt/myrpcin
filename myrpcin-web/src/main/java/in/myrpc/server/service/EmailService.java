/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.User;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 * methods for interacting with the email system
 *
 * @author kguthrie
 */
public interface EmailService {

    void sendVerificationEmail(User user)
            throws AddressException, MessagingException;

}
