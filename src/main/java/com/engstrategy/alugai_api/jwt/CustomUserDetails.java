package com.engstrategy.alugai_api.jwt;

import com.engstrategy.alugai_api.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final String email;
    @Getter
    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isAtivo;
    private final String password; // 1. Adicionado campo para a senha

    public CustomUserDetails(String email, Long userId, Collection<? extends GrantedAuthority> authorities, boolean isAtivo, String password) {
        this.email = email;
        this.userId = userId;
        this.authorities = authorities;
        this.isAtivo = isAtivo;
        this.password = password;
    }

    public CustomUserDetails(Usuario usuario) {
        this.email = usuario.getEmail();
        this.userId = usuario.getId();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
        this.isAtivo = usuario.isAtivo();
        this.password = usuario.getSenha();
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