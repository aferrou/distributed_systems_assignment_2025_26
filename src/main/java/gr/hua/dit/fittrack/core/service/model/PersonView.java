package gr.hua.dit.fittrack.core.service.model;

import gr.hua.dit.fittrack.core.model.PersonType;
import gr.hua.dit.fittrack.core.service.PersonBusinessLogicService;

/**
 * General view of {@link gr.hua.dit.fittrack.core.model.Person} entity.
 *
 * @see gr.hua.dit.fittrack.core.model.Person
 * @see PersonBusinessLogicService
 */
public record PersonView(
        long id,
        String username,
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress,
        PersonType type
) {

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }
}
