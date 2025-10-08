package org.edu.pet.cloud_file_storage.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final long id;
    private final User user;

    public CustomUserDetails(long id, String username, String password, Collection<? extends GrantedAuthority> authorities) {

        this.id = id;
        user = new User(username, password, authorities);
    }

    public CustomUserDetails(long id, String username, String password, boolean enabled,
                             boolean accountNonExpired, boolean credentialsNonExpired,
                             boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {

        this.id = id;
        user = new User(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    @JsonCreator
    private CustomUserDetails(@JsonProperty("id") long id, @JsonProperty("user") User user) {

        this.id = id;
        this.user = user;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return  user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public void eraseCredentials() {
        user.eraseCredentials();
    }
}