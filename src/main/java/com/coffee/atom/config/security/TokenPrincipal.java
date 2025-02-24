package com.coffee.atom.config.security;

import com.coffee.atom.domain.appuser.AppUser;
import lombok.Getter;

import javax.security.auth.Subject;
import java.security.Principal;

@Getter
public class TokenPrincipal implements Principal {

    private final AppUser appUser;
    
    public TokenPrincipal(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public String getName() {
        return appUser.getUsername();
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }
}
