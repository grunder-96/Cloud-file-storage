package org.edu.pet.cloud_file_storage.configuration;

import org.edu.pet.cloud_file_storage.entity.UserEntity;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(@Lazy PasswordEncoder passwordEncoder) {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper
                .typeMap(SignUpRequestDto.class, UserEntity.class)
                .addMappings(mapper -> mapper.using(getEncodePasswordConverter(passwordEncoder)).map(SignUpRequestDto::getPassword, UserEntity::setPassword));

        return modelMapper;
    }

    private Converter<String, String> getEncodePasswordConverter(PasswordEncoder passwordEncoder) {

        return ctx -> ctx.getSource() == null ?
                null : passwordEncoder.encode(ctx.getSource());
    }
}