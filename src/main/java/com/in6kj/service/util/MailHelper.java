package com.in6kj.service.util;

import com.sun.mail.imap.protocol.FLAGS;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by gdpdp on 05.07.2015.
 */

public class MailHelper {

    public List<Email> receiveAndDeleteEmail(String host,
                                             String storeType,
                                             String user,
                                             String password) {
        List<Email> emails = new ArrayList<>();

        try {
            //1) get the session object
            Properties properties = new Properties();
            properties.put("mail.store.protocol", storeType);
            properties.put("mail.pop3s.host", host);
            properties.put("mail.pop3s.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //2) create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");
            store.connect(host, user, password);

            //3) create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            //4) retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            for (Message message : messages) {
                Email email = new Email(InternetAddress.toString(message.getFrom()),
                    message.getSubject(),
                    message.getContent().toString());
                emails.add(email);
                message.setFlag(FLAGS.Flag.DELETED, true);
            }

            //5) close the store and folder objects
            emailFolder.close(true);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emails;
    }
}
