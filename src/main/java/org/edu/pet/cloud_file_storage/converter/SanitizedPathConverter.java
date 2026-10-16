package org.edu.pet.cloud_file_storage.converter;

import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SanitizedPathConverter implements Converter<String, SanitizedPath> {

    @Override
    public SanitizedPath convert(@NonNull String source) {
        return new SanitizedPath(source);
    }
}