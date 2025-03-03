package ru.hse.pensieve.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@Setter
@Getter
public class JwtAuthentication implements Authentication {

    private String username;
    private boolean authenticated;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptySet(); }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getDetails() { return null; }

    @Override
    public Object getPrincipal() { return username; }

    @Override
    public boolean isAuthenticated() { return authenticated; }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() { return username; }

}