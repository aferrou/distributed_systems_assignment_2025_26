package gr.hua.dit.fittrack.core.port.impl.dto;

import java.time.LocalDate;

/**
 * Weather forecast information.
 */
public record WeatherForecast(
        LocalDate date,
        Double temperatureMax,
        Double temperatureMin,
        Double precipitationSum,
        String weatherDescription
) {
    public boolean isSuitableForOutdoorTraining() {
        if (temperatureMax == null || temperatureMin == null || precipitationSum == null) {
            return true; // If no data, assume suitable
        }
        // Consider suitable if:
        // - Not too cold (min temp > 5°C)
        // - Not too hot (max temp < 35°C)
        // - Not too much rain (< 5mm)
        return temperatureMin > 5.0
                && temperatureMax < 35.0
                && precipitationSum < 5.0;
    }
}
