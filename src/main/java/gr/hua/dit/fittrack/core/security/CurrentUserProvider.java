package gr.hua.dit.fittrack.core.security;

import gr.hua.dit.fittrack.core.model.PersonType;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for providing the current user.
 *
 * @see CurrentUser
 */
@Component
public final class CurrentUserProvider {

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(userDetails.personId(), userDetails.getUsername(), userDetails.type()));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser().orElseThrow(() -> new SecurityException("not authenticated"));
    }

    /*
    Person types
     */
    public long requireUserId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.USER) throw new SecurityException("User type/role required");
        return currentUser.id();
    }

    public long requireTrainerId() {
        final CurrentUser currentUser = requireCurrentUser();
        if (currentUser.type() != PersonType.TRAINER) throw new SecurityException("Trainer type/role required");
        return currentUser.id();
    }
}
