package com.foodflow.security;

import com.foodflow.document.UserDocument;
import com.foodflow.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserDocument userDocument;

    public CustomUserDetails(UserDocument userDocument) {
        this.userDocument = userDocument;
    }

    public UserDocument getUserDocument() {
        return userDocument;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userDocument.getRole().name()));
    }

    @Override
    public String getPassword() {
        return userDocument.getPassword();
    }

    @Override
    public String getUsername() {
        return userDocument.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Suspended accounts are locked
        return userDocument.getAccountStatus() != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Only ACTIVE accounts are enabled
        return userDocument.getAccountStatus() == AccountStatus.ACTIVE;
    }
}
