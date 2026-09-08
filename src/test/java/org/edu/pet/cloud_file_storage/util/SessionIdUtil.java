package org.edu.pet.cloud_file_storage.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionIdUtil {

    public static final String SESSION_COOKIE_NAME = "SESSION";
    private static final String REDIS_SESSION_KEY_TEMPLATE = "spring:session:sessions:%s";

    public static String getSessionIdValue(MvcResult mvcResult) {

        return Optional.ofNullable(mvcResult)
                .map(MvcResult::getResponse)
                .map(response -> response.getCookie(SESSION_COOKIE_NAME))
                .map(Cookie::getValue)
                .filter(Predicate.not(String::isBlank))
                .orElseThrow(() -> new IllegalStateException("Unable to extract session id"));
    }

    public static String convertToRedisKey(String cookieSessionId) {

        if (cookieSessionId == null || cookieSessionId.isBlank()) {
            throw new  IllegalStateException("Session id must not be null or blank");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(cookieSessionId);
        String decodedCookieSessionId = new String(decodedBytes, StandardCharsets.UTF_8);

        return REDIS_SESSION_KEY_TEMPLATE.formatted(decodedCookieSessionId);
    }
}