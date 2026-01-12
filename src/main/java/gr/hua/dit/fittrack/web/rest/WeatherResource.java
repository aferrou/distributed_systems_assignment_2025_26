package gr.hua.dit.fittrack.web.rest;

import gr.hua.dit.fittrack.core.model.Appointment;
import gr.hua.dit.fittrack.core.port.WeatherPort;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;
import gr.hua.dit.fittrack.core.repository.AppointmentRepository;
import gr.hua.dit.fittrack.core.security.CurrentUser;
import gr.hua.dit.fittrack.core.security.CurrentUserProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/**
 * REST API για πρόβλεψη καιρού για ραντεβού.
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherResource {

    private final WeatherPort weatherPort;
    private final AppointmentRepository appointmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public WeatherResource(
            final WeatherPort weatherPort,
            final AppointmentRepository appointmentRepository,
            final CurrentUserProvider currentUserProvider) {
        this.weatherPort = weatherPort;
        this.appointmentRepository = appointmentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Παίρνει την πρόβλεψη καιρού για ένα ραντεβού.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getWeatherForAppointment(@PathVariable Long appointmentId) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        final Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Το ραντεβού δεν βρέθηκε"));

        // Έλεγχος ασφαλείας: μόνο ο user που κλείσε το ραντεβού
        if (!appointment.getUser().getId().equals(currentUser.id())) {
            return ResponseEntity.status(403).body(Map.of("error", "Δεν έχετε πρόσβαση σε αυτό το ραντεβού"));
        }

        // Έλεγχος αν το ραντεβού έχει τοποθεσία
        if (appointment.getLatitude() == null || appointment.getLongitude() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Το ραντεβού δεν έχει συντεταγμένες τοποθεσίας",
                    "message", "Για να δείτε τον καιρό, το ραντεβού πρέπει να έχει καθορισμένη τοποθεσία."
            ));
        }

        // Έλεγχος αν το ραντεβού έχει ημερομηνία
        if (appointment.getScheduledAt() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Το ραντεβού δεν έχει προγραμματισμένη ημερομηνία"
            ));
        }

        // Παίρνουμε την ημερομηνία του ραντεβού
        final LocalDate appointmentDate = LocalDate.ofInstant(
                appointment.getScheduledAt(),
                ZoneId.systemDefault()
        );

        try {
            final WeatherForecast forecast = weatherPort.getForecast(
                    appointment.getLatitude(),
                    appointment.getLongitude(),
                    appointmentDate
            );

            return ResponseEntity.ok(Map.of(
                    "date", forecast.date(),
                    "temperatureMax", forecast.temperatureMax() != null ? forecast.temperatureMax() : "N/A",
                    "temperatureMin", forecast.temperatureMin() != null ? forecast.temperatureMin() : "N/A",
                    "precipitationSum", forecast.precipitationSum() != null ? forecast.precipitationSum() : "N/A",
                    "weatherDescription", forecast.weatherDescription() != null ? forecast.weatherDescription() : "Δεν υπάρχουν διαθέσιμα δεδομένα",
                    "isSuitable", forecast.isSuitableForOutdoorTraining(),
                    "location", Map.of(
                            "latitude", appointment.getLatitude(),
                            "longitude", appointment.getLongitude()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Αποτυχία λήψης πρόβλεψης καιρού",
                    "message", "Το weather service μπορεί να μην είναι διαθέσιμο. Βεβαιωθείτε ότι το integration-service τρέχει στο port 8081."
            ));
        }
    }
}
