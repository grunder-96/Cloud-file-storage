package org.edu.pet.cloud_file_storage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionSecurityService {

    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public void applySessionRelatedFunctionality(Authentication auth, HttpServletRequest req, HttpServletResponse resp) {
        sessionAuthenticationStrategy.onAuthentication(auth, req, resp);
    }
}
