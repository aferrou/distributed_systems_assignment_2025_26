package gr.hua.dit.fittrack.core.security;

import gr.hua.dit.fittrack.core.model.PersonType;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */
@SuppressWarnings("RedundantMethodOverride")
public final class ApplicationUserDetails implements UserDetails {

    private final Long personId;
    private final String emailAddress;
    private final String passwordHash;
    private final PersonType personType;

    public ApplicationUserDetails(
            final Long personId,
            final String emailAddress,
            final String passwordHash,
            final PersonType personType
    ) {
        if (personId == null || personId <= 0) throw new IllegalArgumentException("personId must be positive");
        if (emailAddress == null) throw new NullPointerException("emailAddress cannot be null");
        if (emailAddress.isBlank()) throw new IllegalArgumentException("emailAddress cannot be blank");
        if (passwordHash == null) throw new NullPointerException("passwordHash cannot be null");
        // Note: passwordHash can be empty for JWT-authenticated requests
        if (personType == null) throw new NullPointerException("personType cannot be null");

        this.personId = personId;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.personType = personType;
    }

    // Domain-specific accessors
    public Long getPersonId() {
        return personId;
    }

    public PersonType getPersonType() {
        return personType;
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String role;
        if (this.personType == PersonType.TRAINER) role = "ROLE_TRAINER";
        else if (this.personType == PersonType.USER) role = "ROLE_USER";
        else throw new RuntimeException("Invalid type: " + this.personType);
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.emailAddress;
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

    @Override
    public boolean isEnabled() {
        return true;
    }
}
