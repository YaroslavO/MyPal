package com.in6kj.service.util;

import com.sun.mail.pop3.POP3Store;
import com.sun.mail.imap.protocol.FLAGS;
import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Created by gdpdp on 05.07.2015.
 */
public class MailHelper {
//    String host = "mail.javatpoint.com";//change accordingly
//    String mailStoreType = "pop3";
//    String username= "sonoojaiswal@javatpoint.com";
//    String password= "xxxxx";//change accordingly
//
//    receiveEmail(host, mailStoreType, username, password);
    public static void receiveEmail(String pop3Host,
                                    String storeType,
                                    String user,
                                    String password) {
        try {
            //1) get the session object
            Properties properties = new Properties();
            properties.put("mail.pop3.host", pop3Host);
            Session emailSession = Session.getDefaultInstance(properties);

            //2) create the POP3 store object and connect with the pop server
            POP3Store emailStore = (POP3Store) emailSession.getStore(storeType);
            emailStore.connect(user, password);

            //3) create the folder object and open it
            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            //4) retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());
            }

            //5) close the store and folder objects
            emailFolder.close(false);
            emailStore.close();

        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
    }

    public static void deleteEmail(String user,
                                   String password)
        throws Exception {
//        String user= "sonoojaiswal@javatpoint.com";//change accordingly
//        String password="xxxxx";//change accordingly

        //1) get the session object
        Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);

        //2) create the store object and connect to the current host
        Store store = session.getStore("pop3");
        store.connect("mail.javatpoint.com",user,password);

        //3) create the folder object and open it
        Folder folder = store.getFolder("inbox");

        if (!folder.exists()) {
            System.out.println("inbox not found");
            System.exit(0);
        }

        folder.open(Folder.READ_WRITE);

        //4) Get the message to delete
        Message[] msg = folder.getMessages();

        //System.out.println((messages.length+1)+" message found");
        for (int i = 0; i < msg.length; i++) {
            System.out.println("--------- " + (i + 1) + "------------");
            String from = InternetAddress.toString(msg[i].getFrom());

            if (from != null) {
                System.out.println("From: " + from);
            }

            String replyTo = InternetAddress.toString(
                msg[i].getReplyTo());
            if (replyTo != null) {
                System.out.println("Reply-to: " + replyTo);
            }

            String to = InternetAddress.toString(
                msg[i].getRecipients(Message.RecipientType.TO));

            if (to != null) {
                System.out.println("To: " + to);
            }

            String subject = msg[i].getSubject();
            if (subject != null) {
                System.out.println("Subject: " + subject);
            }
            Date sent = msg[i].getSentDate();
            if (sent != null) {
                System.out.println("Sent: " + sent);
            }
            System.out.println("Message : ");
            System.out.println(msg[i].getContent());

        }//end of for loop

        // get the message number to delete (optional)
        System.out.println("Enter message number to delete :");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String no = br.readLine();
        //5) delete the message using setFlag method
        msg[Integer.parseInt(no) - 1].setFlag(FLAGS.Flag.DELETED, true);

        System.out.println("Message Deleted .....");

        folder.close(true);
        store.close();
    }
}
