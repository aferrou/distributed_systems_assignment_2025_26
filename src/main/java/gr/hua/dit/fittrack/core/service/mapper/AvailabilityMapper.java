package gr.hua.dit.fittrack.core.service.mapper;

import gr.hua.dit.fittrack.core.model.Availability;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.service.model.AvailabilityRequest;
import gr.hua.dit.fittrack.core.service.model.AvailabilityResponse;
import jdk.jfr.Category;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {
    public AvailabilityResponse toResponseDto(Availability availability) {
        if (availability == null) {
            return null;
        }

        return new AvailabilityResponse(
                availability.getId(),
                availability.getStartTime(),
                availability.getEndTime()
        );
    }

    // -------------------------------
    // Request DTO → Entity
    // -------------------------------
    public Availability toEntity(
            AvailabilityRequest dto,
            Person trainer
    ) {
        if (dto == null || trainer == null) {
            return null;
        }

        return new Availability(
                dto.startTime(),
                dto.endTime(),
                trainer
        );
    }
}