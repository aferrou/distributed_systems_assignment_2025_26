package gr.hua.dit.fittrack.core.port.impl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Response from external weather service.
 */
public record WeatherForecastResponse(
        LocalDate date,
        @JsonProperty("temperature_max") double temperatureMax,
        @JsonProperty("temperature_min") double temperatureMin,
        @JsonProperty("precipitation_sum") double precipitationSum,
        @JsonProperty("weather_description") String weatherDescription
) {
}