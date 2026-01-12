package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.service.model.CreatePersonRequest;
import gr.hua.dit.fittrack.core.service.model.CreatePersonResult;

/**
 * Service for managing {@link gr.hua.dit.fittrack.core.model.Person}.
 */
public interface PersonBusinessLogicService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        return this.createPerson(createPersonRequest, true);
    }

    /**
     * Creates a trainer account. Only accessible by admins.
     *
     * @param createPersonRequest the trainer details
     * @param notify whether to send SMS notification
     * @return result of the creation
     */
    CreatePersonResult createTrainer(CreatePersonRequest createPersonRequest, boolean notify);
}
