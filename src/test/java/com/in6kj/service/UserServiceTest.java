package com.in6kj.service;

import com.in6kj.Application;
import com.in6kj.domain.Authority;
import com.in6kj.domain.PersistentToken;
import com.in6kj.domain.User;
import com.in6kj.repository.PersistentTokenRepository;
import com.in6kj.repository.UserRepository;
import com.in6kj.service.util.RandomUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@Transactional
public class UserServiceTest {

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;


  /*  @Test
    public void testRemoveOldPersistentTokens() {
        User admin = userRepository.findOneByLogin("admin");
        int existingCount = persistentTokenRepository.findByUser(admin).size();
        generateUserToken(admin, "1111-1111", new LocalDate());
        LocalDate now = new LocalDate();
        generateUserToken(admin, "2222-2222", now.minusDays(32));
        assertThat(persistentTokenRepository.findByUser(admin)).hasSize(existingCount + 2);
        userService.removeOldPersistentTokens();
        assertThat(persistentTokenRepository.findByUser(admin)).hasSize(existingCount + 1);
    }*/

    @Test
    public void assertThatUserMustExistToResetPassword() {

        User user = userService.requestPasswordReset("john.doe@localhost");
        assertThat(user).isNull();

        user = userService.requestPasswordReset("admin@localhost");
        assertThat(user).isNotNull();
        assertThat(user.getLogin()).isEqualTo("admin@localhost");
        assertThat(user.getResetDate()).isNotNull();
        assertThat(user.getResetKey()).isNotNull();

    }

    @Test
    public void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        User user = userService.createUserInformation("john.doe@localhost", "johndoe", "John", "Doe", "en-US");
        User maybeUser = userService.requestPasswordReset("john.doe@localhost");
        assertThat(maybeUser).isNull();
        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {

        User user = userService.createUserInformation("john.doe@localhost",  "johndoe", "John", "Doe", "en-US");

        DateTime daysAgo = DateTime.now().minusHours(25);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);

        userRepository.save(user);

        User maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());

        assertThat(maybeUser).isNull();

        userRepository.delete(user);

    }

    @Test
    public void assertThatResetKeyMustBeValid() {

        User user = userService.createUserInformation("john.doe@localhost", "johndoe", "John", "Doe",  "en-US");

        DateTime daysAgo = DateTime.now().minusHours(25);
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");

        userRepository.save(user);

        User maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());

        assertThat(maybeUser).isNull();

        userRepository.delete(user);

    }

    @Test
    public void assertThatUserCanResetPassword() {

        User user = userService.createUserInformation("john.doe@localhost", "johndoe", "John", "Doe", "en-US");

        String oldPassword = user.getPassword();

        DateTime daysAgo = DateTime.now().minusHours(2);
        String resetKey = RandomUtil.generateResetKey();
        user.setActivated(true);
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);

        userRepository.save(user);

        User maybeUser = userService.completePasswordReset("johndoe2", user.getResetKey());

        assertThat(maybeUser).isNotNull();
        assertThat(maybeUser.getResetDate()).isNull();
        assertThat(maybeUser.getResetKey()).isNull();
        assertThat(maybeUser.getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);

    }

    @Test
    public void testFindNotActivatedUsersByCreationDateBefore() {
        userService.removeNotActivatedUsers();
        DateTime now = new DateTime();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        assertThat(users).isEmpty();
    }

    private void generateUserToken(User user, String tokenSeries, LocalDate localDate) {
        PersistentToken token = new PersistentToken();
        token.setSeries(tokenSeries);
        token.setUser(user);
        token.setTokenValue(tokenSeries + "-data");
        token.setTokenDate(localDate);
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);
    }

    @Test
    public void AdminCreateUserWithRole__Role_User() throws Exception {
        // given
        User user = userService.createUserInformationByAdmin("google@gamil.com", "Joshn", "Foo");
        Authority authority = user.getAuthorities().iterator().next();

        // when
        String role = authority.getName();


        // then
        Assert.assertThat(role, is("ROLE_USER"));
    }

    @Test
    public void checkingPasswordNewUserCreateByAdmin() throws Exception {
        //given
        User user = userService.createUserInformationByAdmin("google@gamil.com", "Joshn", "Foo");

        //when
        String actualPassword = user.getPassword();
        String expectPass = "12345";
        String expectPassEncoded = passwordEncoder.encode(expectPass);

        //then
        Assert.assertThat(actualPassword, not(expectPassEncoded));
    }

    @Test
    public void checkCreateNewUserByAdmin() throws Exception {
        //given
        userService.createUserInformationByAdmin("google@gamil.com", "Joshn", "Foo");

        //when
        User userFromDB = userRepository.findOneByLogin("google@gamil.com");
        Long userId = userFromDB.getId();

        //then
        Assert.assertNotNull(userId);
        Assert.assertThat(userId, is(5L));
    }

    @Test
    public void checkBalanceNewUserCreateByAdmin() throws Exception {
        //given
        userService.createUserInformationByAdmin("google@gmail.com","Joshn","Jonson");

        //when
        User userFromDB = userRepository.findOneByLogin("google@gmail.com");
        BigDecimal balance = userFromDB.getBalance();

        //then
        Assert.assertNotNull(balance);
        Assert.assertThat(balance, is(BigDecimal.valueOf(0)));
    }
}
