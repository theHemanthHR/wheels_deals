package com.ecom.proj.security;

import com.ecom.proj.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}
    public static AuthPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthPrincipal p))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return p;
    }
    public static Long id() { return get().id(); }
}
