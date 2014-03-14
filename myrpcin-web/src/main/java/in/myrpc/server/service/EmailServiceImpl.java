/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.server.service;

import in.myrpc.shared.model.User;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.orgama.shared.auth.model.AuthUser;

/**
 * implementation of the email service interface
 *
 * @author kguthrie
 */
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(User user)
            throws AddressException, MessagingException {

        AuthUser authUser = user.getAuthUserRef().get();
        String emailAddress = authUser.getSanitizedEmailAddress();
        String verificationCode = user.getVerificationCode();
        String verificationUrl = "http://myrpcin.appspot.com/r/verify?code="
                + verificationCode;

        InternetAddress recipient = new InternetAddress(emailAddress);

        String message = ""
                + "Thank you for your signing up for an account on "
                + "MyRpc.in; click the link below to verify your email address "
                + "and start connecting to your devices<br/><br/>"
                + "<a href='" + verificationUrl + "'>" + verificationUrl
                + "</a><br/>";

        sendEmail(recipient, "MyRpc.in Verification Email", message);
    }

    private void sendEmail(InternetAddress recipient, String subject,
            String body) throws MessagingException, AddressException {
        sendEmail(new InternetAddress[]{recipient}, subject, body);
    }

    private void sendEmail(InternetAddress[] recipients, String subject,
            String body) throws MessagingException, AddressException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress("kevin.guthrie@gmail.com",
                    "MyRpc.in Robot"));
        }
        catch (UnsupportedEncodingException ue) {
            throw new MessagingException("Failed to create sender address", ue);
        }

        msg.addRecipients(Message.RecipientType.TO, recipients);
        msg.setSubject(subject);
        msg.setContent(body, "text/html; charset=utf-8");

        Transport.send(msg);
    }

}
