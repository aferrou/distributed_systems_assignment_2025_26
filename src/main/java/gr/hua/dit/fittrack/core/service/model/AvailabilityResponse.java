package gr.hua.dit.fittrack.core.service.model;

import gr.hua.dit.fittrack.core.model.Availability;

import java.time.OffsetDateTime;

public record AvailabilityResponse(
        Long id,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
    public AvailabilityResponse(Availability availability) {
        this(
                availability.getId(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }
}
