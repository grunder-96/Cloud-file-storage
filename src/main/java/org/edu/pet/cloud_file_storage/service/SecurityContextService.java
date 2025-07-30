package org.edu.pet.cloud_file_storage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final SecurityContextRepository securityContextRepository;

    public void injectSecurityContext(Authentication authResponse, HttpServletRequest req, HttpServletResponse resp) {

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authResponse);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, req, resp);
    }
}