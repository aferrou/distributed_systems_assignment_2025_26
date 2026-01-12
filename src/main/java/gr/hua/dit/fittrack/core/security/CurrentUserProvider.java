package gr.hua.dit.fittrack.core.security;

import gr.hua.dit.fittrack.core.model.PersonType;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for providing access to the current user.
 *
 * @see CurrentUser
 */
@Component
public final class CurrentUserProvider {

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(userDetails.getPersonId(), userDetails.getUsername(), userDetails.getPersonType()));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new SecurityException("User is not authenticated"));
    }

    public long requireUserId() {
        final CurrentUser currentUser = requireCurrentUser();
        if (currentUser.type() != PersonType.USER) {
            throw new SecurityException("USER role required");
        }
        return currentUser.id();
    }

    public long requireTrainerId() {
        final CurrentUser currentUser = requireCurrentUser();
        if (currentUser.type() != PersonType.TRAINER) {
            throw new SecurityException("TRAINER role required");
        }
        return currentUser.id();
    }
}