package gr.hua.dit.fittrack.core.service;

import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.service.model.AvailabilityRequest;
import gr.hua.dit.fittrack.core.service.model.AvailabilityResponse;

import java.util.List;

public interface AvailabilityService {
    List<AvailabilityResponse> getTrainerAvailability(Person trainer);

    AvailabilityResponse createAvailability(Person trainer, AvailabilityRequest dto);

    void updateAvailability(Person trainer, Long id, AvailabilityRequest dto);

    String deleteAvailability(Person trainer, Long id);

    boolean isOverlapping(Person trainer, AvailabilityRequest dto, Long ignoreId);
}
