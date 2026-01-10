package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.service.model.PersonView;

import java.util.List;

/**
 * Service for managing {@code Person} for data analytics purposes.
 */
public interface PersonDataService {

    List<PersonView> getAllPeople();
}
