package org.edu.pet.cloud_file_storage.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserDataUtil {

    public static SignUpRequestDto getJohnDoeRegData() {
        return getSignUpDto(TestUser.JOHN_DOE);
    }

    public static SignInRequestDto getJohnDoeLoginData() {
        return getSignInDto(TestUser.JOHN_DOE);
    }

    public static SignUpRequestDto getJackMarstonRegData() {
        return getSignUpDto(TestUser.JACK_MARSTON);
    }

    public static SignInRequestDto getJackMarstonLoginData() {
        return getSignInDto(TestUser.JACK_MARSTON);
    }

    private static SignUpRequestDto getSignUpDto(TestUser testUser) {
        return  new SignUpRequestDto(testUser.name, testUser.password);
    }

    private static SignInRequestDto getSignInDto(TestUser testUser) {
        return  new SignInRequestDto(testUser.name, testUser.password);
    }

    @RequiredArgsConstructor
    private enum TestUser {

        JOHN_DOE("john_doe", "Qwe1234!"),
        JACK_MARSTON("jack_marston", "jackM345!");

        private final String name;
        private final String password;
    }
}