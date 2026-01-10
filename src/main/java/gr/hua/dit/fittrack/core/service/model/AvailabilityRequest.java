package gr.hua.dit.fittrack.core.service.model;

import java.time.OffsetDateTime;

public record AvailabilityRequest(
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {}
