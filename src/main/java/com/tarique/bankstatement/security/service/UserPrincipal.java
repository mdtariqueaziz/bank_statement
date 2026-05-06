package com.tarique.bankstatement.security.service;

import com.tarique.bankstatement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal wrapping our {@link User} entity.
 * Mirrors the pattern from the reference my-project.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;
	private final Long id;
    private final String username;
    private final String email;
    private final String role;
    private final String password;
    private final boolean enabled;

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getPassword(),
                user.isEnabled()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return enabled; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return enabled; }
}
