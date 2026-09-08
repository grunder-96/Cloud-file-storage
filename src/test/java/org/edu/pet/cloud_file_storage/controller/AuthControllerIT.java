package org.edu.pet.cloud_file_storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.edu.pet.cloud_file_storage.config.MinioTestConfig;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.repository.UserRepository;
import org.edu.pet.cloud_file_storage.service.UserService;
import org.edu.pet.cloud_file_storage.util.SessionIdUtil;
import org.edu.pet.cloud_file_storage.util.UserDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Import(MinioTestConfig.class)
@Transactional
public class AuthControllerIT extends AbstractAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String REG_URL = "/api/auth/sign-up";
    private static final String LOGIN_URL = "/api/auth/sign-in";
    private static final String LOGOUT_URL = "/api/auth/sign-out";


    @BeforeEach
    public void createTestUser() {
        userService.create(UserDataUtil.getJohnDoeRegData());
    }

    @Nested
    @DisplayName("Registration endpoints")
    class RegistrationIT {

        @Test
        public void givenValidRegData_whenRegister_thenAccountAndSessionCreated() throws Exception {
            //given
            SignUpRequestDto validRegData = UserDataUtil.getJackMarstonRegData();
            //when
            MvcResult mvcResult = mockMvc.perform(post(REG_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegData)))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isCreated(),
                            content().contentTypeCompatibleWith(APPLICATION_JSON),
                            jsonPath("$.username", is(validRegData.getUsername())),
                            cookie().exists(SessionIdUtil.SESSION_COOKIE_NAME)
                    )
                    .andReturn();

            assertThat(userRepository.findByUsernameIgnoreCase(validRegData.getUsername())).isPresent();

            String cookieSessionId = SessionIdUtil.getSessionIdValue(mvcResult);
            assertTrue(stringRedisTemplate.hasKey(SessionIdUtil.convertToRedisKey(cookieSessionId)));
        }

        @Test
        public void givenDuplicateRegData_whenRegister_thenReturnErrorResponse() throws Exception {
            //given
            SignUpRequestDto regData = UserDataUtil.getJohnDoeRegData();
            //when
            mockMvc.perform(post(REG_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regData)))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isConflict(),
                            content().contentTypeCompatibleWith(APPLICATION_JSON),
                            jsonPath("$.message", is("Username is already taken"))
                    );
        }

        @Test
        public void givenRegDataWithInvalidUsername_whenRegister_thenReturnErrorResponse() throws Exception {
            //given
            SignUpRequestDto regData = new SignUpRequestDto("$.gnom", "Glls1@_35");
            //when
            mockMvc.perform(post(REG_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(regData)))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isBadRequest(),
                            content().contentTypeCompatibleWith(APPLICATION_JSON),
                            jsonPath("$.message.username", contains("must start with a letter and end with a letter or digit, contain only a-zA-Z0-9_-"))
                    );
        }
    }

    @Nested
    @DisplayName("Login endpoints")
    class LoginIT {

        @Test
        public void givenNonexistentUserData_whenLogin_thenErrorResponse() throws Exception {
            //given
            SignInRequestDto user = UserDataUtil.getJackMarstonLoginData();
            //when
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isUnauthorized(),
                            content().contentTypeCompatibleWith(APPLICATION_JSON),
                            jsonPath("$.message", is("There is no such user or the password is incorrect"))
                    );
        }

        @Test
        public void givenAuthenticatedUser_whenAgainLogin_thenCreateNewSession() throws Exception {
            //given
            SignInRequestDto alreadyRegisteredUser = UserDataUtil.getJohnDoeLoginData();

            MvcResult firstLoginResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(alreadyRegisteredUser)))
                    .andDo(print())
                    .andReturn();

            String firstLoginSessionId = SessionIdUtil.getSessionIdValue(firstLoginResult);
            assertTrue(stringRedisTemplate.hasKey(SessionIdUtil.convertToRedisKey(firstLoginSessionId)),
                    "Session should exist in Redis for authenticated user");
            //when
            MvcResult secondLoginResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(alreadyRegisteredUser))
                            .cookie(new Cookie(SessionIdUtil.SESSION_COOKIE_NAME, firstLoginSessionId)))
                    .andDo(print())
            //then
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            Map.of("username", alreadyRegisteredUser.getUsername()))))
                    .andReturn();

            String secondLoginSessionId = SessionIdUtil.getSessionIdValue(secondLoginResult);
            assertNotEquals(firstLoginSessionId, secondLoginSessionId);

            assertFalse(stringRedisTemplate.hasKey(SessionIdUtil.convertToRedisKey(firstLoginSessionId)));
            assertTrue(stringRedisTemplate.hasKey(SessionIdUtil.convertToRedisKey(secondLoginSessionId)));
        }
    }

    @Nested
    @DisplayName("Logout endpoints")
    class LogoutIT {

        @Test
        public void givenUnauthorizedUser_whenLogout_thenErrorResponse() throws Exception {
            //given

            //when
            mockMvc.perform(post(LOGOUT_URL))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isUnauthorized(),
                            jsonPath("$.message", is("Logout is available only for authenticated users"))
                    );
        }

        @Test
        public void givenAuthorizedUser_whenLogout_thenInvalidateSessionAndDeleteCookie() throws Exception {
            //given
            SignInRequestDto user = UserDataUtil.getJohnDoeLoginData();

            MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user)))
                    .andDo(print())
                    .andReturn();

            String sessionId = SessionIdUtil.getSessionIdValue(loginResult);
            String sessionRedisKey = SessionIdUtil.convertToRedisKey(sessionId);
            assertTrue(stringRedisTemplate.hasKey(sessionRedisKey), "Session should exist in Redis for authenticated user");
            //when
            mockMvc.perform(post(LOGOUT_URL)
                            .cookie(new Cookie(SessionIdUtil.SESSION_COOKIE_NAME, sessionId)))
                    .andDo(print())
            //then
                    .andExpectAll(
                            status().isNoContent(),
                            cookie().exists(SessionIdUtil.SESSION_COOKIE_NAME),
                            cookie().value(SessionIdUtil.SESSION_COOKIE_NAME, is(emptyString())),
                            cookie().maxAge(SessionIdUtil.SESSION_COOKIE_NAME, 0)
                    );

            assertFalse(stringRedisTemplate.hasKey(sessionRedisKey));
        }
    }
}