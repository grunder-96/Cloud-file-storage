package org.edu.pet.cloud_file_storage.controller;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.AuthResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final ModelMapper modelMapper;

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> showMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(modelMapper.map(userDetails, AuthResponseDto.class));
    }
}