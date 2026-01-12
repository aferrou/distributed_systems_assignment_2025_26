package gr.hua.dit.fittrack.core.service.impl;

import gr.hua.dit.fittrack.core.model.Availability;
import gr.hua.dit.fittrack.core.model.Person;
import gr.hua.dit.fittrack.core.repository.AvailabilityRepository;
import gr.hua.dit.fittrack.core.service.AvailabilityService;
import gr.hua.dit.fittrack.core.service.mapper.AvailabilityMapper;
import gr.hua.dit.fittrack.core.service.model.AvailabilityRequest;
import gr.hua.dit.fittrack.core.service.model.AvailabilityResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {
    private final AvailabilityRepository repository;
    private final AvailabilityMapper mapper;

    public AvailabilityServiceImpl(AvailabilityRepository repository, AvailabilityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<AvailabilityResponse> getTrainerAvailability(Person trainer) {
        return repository.findByTrainer(trainer)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    public AvailabilityResponse createAvailability(Person trainer, AvailabilityRequest dto) {
        if (dto.startTime().isBefore(OffsetDateTime.now()))
            throw new IllegalArgumentException("Cannot create availability in the past");
        if (!dto.endTime().isAfter(dto.startTime()))
            throw new IllegalArgumentException("End time must be after start time");
        if (isOverlapping(trainer, dto, null))
            throw new IllegalArgumentException("This slot overlaps with existing availability");

        Availability saved = repository.save(mapper.toEntity(dto, trainer));
        return mapper.toResponseDto(saved);
    }

    @Override
    public void updateAvailability(Person trainer, Long id, AvailabilityRequest dto) {
        Availability existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found"));

        if (!existing.getTrainer().getId().equals(trainer.getId()))
            throw new IllegalArgumentException("Availability not found");

        if (dto.startTime().isBefore(OffsetDateTime.now()))
            throw new IllegalArgumentException("Cannot create availability in the past");

        if (!dto.endTime().isAfter(dto.startTime()))
            throw new IllegalArgumentException("End time must be after start time");

        if (isOverlapping(trainer, dto, id))
            throw new IllegalArgumentException("This slot overlaps with existing availability");

        existing.setStartTime(dto.startTime());
        existing.setEndTime(dto.endTime());
        repository.save(existing);
    }

    @Override
    public boolean isOverlapping(Person trainer, AvailabilityRequest dto, Long ignoreId) {
        return repository.findByTrainer(trainer)
                .stream()
                .anyMatch(a -> !a.getId().equals(ignoreId) &&
                        dto.startTime().isBefore(a.getEndTime()) &&
                        dto.endTime().isAfter(a.getStartTime()));
    }

    @Override
    public String deleteAvailability(Person trainer, Long id) {
        Availability existing = repository.findById(id).orElse(null);
        if (existing == null || !existing.getTrainer().getId().equals(trainer.getId()))
            return "Error: Availability not found";

        repository.delete(existing);
        return "Deleted";
    }
}
