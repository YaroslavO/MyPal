package com.in6kj.service;

import com.in6kj.domain.Authority;
import com.in6kj.domain.PersistentToken;
import com.in6kj.domain.User;
import com.in6kj.repository.AuthorityRepository;
import com.in6kj.repository.PersistentTokenRepository;
import com.in6kj.repository.UserRepository;
import com.in6kj.security.SecurityUtils;
import com.in6kj.service.util.RandomUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private MailService mailService;

    public  User activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        User user = userRepository.findOneByActivationKey(key);
        if (user != null) {
            user.setActivated(true);
            user.setActivationKey(null);
            userRepository.save(user);
            log.debug("Activated user: {}", user);
        }
        return user;
    }

    public User completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        User user = userRepository.findOneByResetKey(key);
        DateTime oneDayAgo = DateTime.now().minusHours(24);
        if (user != null && user.getActivated()) {
            if (user.getResetDate().isAfter(oneDayAgo.toInstant().getMillis())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                userRepository.save(user);
                return user;
            } else {
                return null;
            }
        }
        return null;
    }

    public User requestPasswordReset(String login) {
        User user = userRepository.findOneByLogin(login);
        if (user != null && user.getActivated()) {
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(DateTime.now());
            userRepository.save(user);
            return user;
        }
        return null;
    }

    public User createUserInformation(String login, String password, String firstName, String lastName,
                                      String langKey) {

        User newUser = new User();
        Authority authority = authorityRepository.findOne("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setBalance(BigDecimal.ZERO);
        newUser.setLangKey(langKey);
        newUser.setActivated(false);
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUserInformationByAdmin(String login, String firstName, String lastName) {
        User newUser = new User();
        Authority authority = authorityRepository.findOne("ROLE_USER");
        Set<Authority> authorities = new HashSet<>();
        String generatedPassword = RandomUtil.generatePassword();
        String encryptedPassword = passwordEncoder.encode(generatedPassword);
        newUser.setLogin(login);
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setBalance(BigDecimal.ZERO);
        newUser.setLangKey("en");
        newUser.setActivated(true);
        newUser.setActivationKey(null);
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created by Admin Information for User : {}", newUser);
        mailService.sendNotificationEmail(newUser, generatedPassword, newUser.getLogin());

        return newUser;
    }

    public void updateUserInformation(String firstName, String lastName, BigDecimal balance, String login, String langKey) {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin());
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setBalance(balance);
        currentUser.setLogin(login);
        currentUser.setLangKey(langKey);
        userRepository.save(currentUser);
        log.debug("Changed Information for User: {}", currentUser);
    }

    public void changePassword(String password) {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin());
        String encryptedPassword = passwordEncoder.encode(password);
        currentUser.setPassword(encryptedPassword);
        userRepository.save(currentUser);
        log.debug("Changed password for User: {}", currentUser);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        User currentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentLogin());
        currentUser.getAuthorities().size(); // eagerly load the association
        return currentUser;
    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at midnight.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = new LocalDate();
        List<PersistentToken> tokens = persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1));
        for (PersistentToken token : tokens) {
            log.debug("Deleting token {}", token.getSeries());
            User user = token.getUser();
            user.getPersistentTokens().remove(token);
            persistentTokenRepository.delete(token);
        }
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p/>
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        DateTime now = new DateTime();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }
}
