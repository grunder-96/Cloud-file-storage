package org.edu.pet.cloud_file_storage.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.auth.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.dto.auth.UserResponseDto;
import org.edu.pet.cloud_file_storage.entity.UserEntity;
import org.edu.pet.cloud_file_storage.exception.UsernameAlreadyTakenException;
import org.edu.pet.cloud_file_storage.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USERNAME_UNIQUE_INDEX_NAME = "idx_users_username_lower";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto create(SignUpRequestDto signUpRequestDto) {

        UserEntity userForSave = UserEntity.builder()
                .username(signUpRequestDto.username())
                .password(passwordEncoder.encode(signUpRequestDto.password()))
                .build();

        try {
            userForSave = userRepository.saveAndFlush(userForSave);
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityViolationException(e);
        }

        return new UserResponseDto(userForSave.getId(),  userForSave.getUsername());
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsernameIgnoreCase(String username) {
        return  userRepository.findByUsernameIgnoreCase(username);
    }

    private void handleDataIntegrityViolationException(DataIntegrityViolationException e) {

        Throwable cause = e.getCause();

        if (cause instanceof ConstraintViolationException) {
            if (cause.getMessage().contains(USERNAME_UNIQUE_INDEX_NAME)) {
                throw new UsernameAlreadyTakenException("Username is already taken", e);
            }
        }

        throw e;
    }
}