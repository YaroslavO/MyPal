package com.in6kj.web.rest;

import com.in6kj.Application;
import com.in6kj.domain.Authority;
import com.in6kj.domain.User;
import com.in6kj.repository.AuthorityRepository;
import com.in6kj.repository.UserRepository;
import com.in6kj.security.AuthoritiesConstants;
import com.in6kj.service.MailService;
import com.in6kj.service.UserService;
import com.in6kj.web.rest.dto.UserDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class AccountResourceTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private UserService userService;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail((User) anyObject(), anyString());

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        ReflectionTestUtils.setField(accountResource, "mailService", mockMailService);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userRepository", userRepository);
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);
        ReflectionTestUtils.setField(accountUserMockResource, "mailService", mockMailService);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
                .with(new RequestPostProcessor() {
                    public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                        request.setRemoteUser("test");
                        return request;
                    }
                })
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("test"));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        User user = new User();

        user.setFirstName("john");
        user.setLastName("doe");
        user.setLogin("john.doe@jhipter.com");
        user.setAuthorities(authorities);
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restUserMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login").value("john.doe@jhipter.com"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.roles").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        UserDTO u = new UserDTO(
            "joe@example.com",
            "password",
            "Joe",
            new BigDecimal(10),
            "Shmoe",
            "en",
            Arrays.asList(AuthoritiesConstants.USER)
        );

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(u)))
            .andExpect(status().isCreated());

        User user = userRepository.findOneByLogin("joe@example.com");
        assertThat(user).isNotNull();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        UserDTO u = new UserDTO(
            "funky",    // e-mail
            "password",             // password
            "Funky",                // firstName
            new BigDecimal(10),
            "One",                  // lastName
            "en",                   // langKey
            Arrays.asList(AuthoritiesConstants.USER)
        );

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(u)))
            .andExpect(status().isBadRequest());

        User user = userRepository.findOneByLogin("funky@example.com");
        assertThat(user).isNull();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        UserDTO u = new UserDTO(
            "alice@example.com",    // e-mail
            "password",             // password
            "Alice",                // firstName
            new BigDecimal(10),
            "Something",            // lastName
            "en",                   // langKey
            Arrays.asList(AuthoritiesConstants.USER)
        );

        // Duplicate login
        UserDTO dup = new UserDTO(
            "alice@example.com",
            u.getPassword(),
            u.getFirstName(),
            u.getBalance(),
            u.getLastName(),
            u.getLangKey(),
            u.getRoles());

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(u)))
            .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(dup)))
            .andExpect(status().is4xxClientError());

        User userDup = userRepository.findOneByLogin("alicejr@example.com");
        assertThat(userDup).isNull();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        UserDTO u = new UserDTO(
            "badguy@example.com",               // login
            "password",             // password
            "Bad",                  // firstName
            new BigDecimal(10),   // balance
            "Guy",                  // lastName
            "en",                   // langKey
            Arrays.asList(AuthoritiesConstants.ADMIN) // <-- only admin should be able to do that
        );

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(u)))
            .andExpect(status().isCreated());

        User userDup = userRepository.findOneByLogin("badguy@example.com");
        assertThat(userDup).isNotNull();
        assertThat(userDup.getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER));
    }
}
