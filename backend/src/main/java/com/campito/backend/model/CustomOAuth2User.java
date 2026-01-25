package com.campito.backend.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OidcUser {

    private final OidcUser oidcUser;
    private final Usuario usuario;

    // Constructor para OAuth2 con OidcUser
    public CustomOAuth2User(OidcUser oidcUser, Usuario usuario) {
        this.oidcUser = oidcUser;
        this.usuario = usuario;
    }

    // Constructor simplificado para JWT (sin OidcUser)
    public CustomOAuth2User(Map<String, Object> attributes, String nameAttributeKey, Usuario usuario) {
        this.oidcUser = null;
        this.usuario = usuario;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser != null ? oidcUser.getAttributes() : Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oidcUser != null ? oidcUser.getAuthorities() : List.of();
    }

    @Override
    public String getName() {
        return oidcUser != null ? oidcUser.getName() : usuario.getEmail();
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser != null ? oidcUser.getClaims() : Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser != null ? oidcUser.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser != null ? oidcUser.getIdToken() : null;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
