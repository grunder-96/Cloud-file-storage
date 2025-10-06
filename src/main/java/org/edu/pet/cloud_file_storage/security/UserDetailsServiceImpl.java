package org.edu.pet.cloud_file_storage.security;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        return userService.findByUsernameIgnoreCase(username)
                .map(u -> new CustomUserDetails(
                        u.getId(),
                        u.getUsername(),
                        u.getPassword(),
                        Collections.emptyList()
                ))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}