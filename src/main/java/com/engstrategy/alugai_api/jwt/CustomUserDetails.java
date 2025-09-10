package com.engstrategy.alugai_api.jwt;

import com.engstrategy.alugai_api.model.Usuario;
import com.engstrategy.alugai_api.model.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    private final String email;
    @Getter
    private final UUID userId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isAtivo;
    private final String password;
    @Getter
    private final Role role;

    public CustomUserDetails(String email, UUID userId, Collection<? extends GrantedAuthority> authorities, boolean isAtivo, String password, Role role) {
        this.email = email;
        this.userId = userId;
        this.authorities = authorities;
        this.isAtivo = isAtivo;
        this.password = password;
        this.role = role;
    }

    public CustomUserDetails(Usuario usuario) {
        this.email = usuario.getEmail();
        this.userId = usuario.getId();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
        this.isAtivo = usuario.isAtivo();
        this.password = usuario.getSenha();
        this.role = usuario.getRole();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isEnabled() {
        return this.isAtivo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}