package org.edu.pet.cloud_file_storage.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUserDetails extends User {

    @Getter
    private final Long id;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        Assert.isTrue(id != null,"Cannot pass null or empty values to constructor");
        this.id = id;
    }

    @JsonCreator
    public CustomUserDetails(@JsonProperty("id") Long id,
                             @JsonProperty("username") String username,
                             @JsonProperty("password") String password,
                             @JsonProperty("enabled") boolean enabled,
                             @JsonProperty("accountNonExpired") boolean accountNonExpired,
                             @JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
                             @JsonProperty("accountNonLocked") boolean accountNonLocked,
                             @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities) {

        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        Assert.isTrue(id != null,"Cannot pass null or empty values to constructor");
        this.id = id;
    }
}