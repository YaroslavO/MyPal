package com.in6kj.service.util;

import com.in6kj.Application;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by employee on 7/6/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@Transactional
public class MailHelperTest {

    @Inject
    private Environment env;

    private String host = "";

    private String storeType = "";

    private String user = "";

    private String password = "";

    private MailHelper mailHelper;

    @Before
    public void setUp() throws Exception {
        mailHelper = new MailHelper();
        this.host = env.getProperty("mail.host2");
        this.storeType = env.getProperty("mail.storetype");
        this.user = env.getProperty("mail.username");
        this.password = env.getProperty("mail.password");
    }

    @Test
    public void receiveAndDeleteAllMailInInboxFolder() throws Exception {
        List<Email> emails = mailHelper.receiveAndDeleteEmail(host, storeType, user, password);

        Assert.assertNotNull(emails);
    }
}
