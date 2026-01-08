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
        Long id,
        String firstName,
        String lastName,
        String emailAddress,
        String mobilePhoneNumber,
        PersonType type
) {

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }
}