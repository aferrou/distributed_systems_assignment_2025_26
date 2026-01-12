package gr.hua.dit.fittrack.core.port.impl;

import gr.hua.dit.fittrack.core.port.WeatherPort;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;

/**
 * Implementation of WeatherPort using the external weather service.
 */
@Component
public class WeatherPortImpl implements WeatherPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherPortImpl.class);

    private final RestClient restClient;
    private final String weatherServiceUrl;
    private final boolean weatherServiceEnabled;

    public WeatherPortImpl(
            final RestClient restClient,
            @Value("${fittrack.integration.weather-service-url:http://localhost:8081}") final String weatherServiceUrl,
            @Value("${fittrack.integration.weather-service-enabled:false}") final boolean weatherServiceEnabled) {
        if (restClient == null) throw new NullPointerException();
        if (weatherServiceUrl == null || weatherServiceUrl.isBlank()) {
            throw new IllegalArgumentException("Weather service URL cannot be null or blank");
        }

        this.restClient = restClient;
        this.weatherServiceUrl = weatherServiceUrl;
        this.weatherServiceEnabled = weatherServiceEnabled;

        if (!weatherServiceEnabled) {
            LOGGER.warn("Weather service is disabled, will return default forecasts");
        } else {
            LOGGER.info("Weather service ENABLED at: {}", weatherServiceUrl);
        }
    }

    @Override
    @Cacheable(value = "weatherForecasts", key = "#latitude + '_' + #longitude + '_' + #date")
    public WeatherForecast getForecast(final double latitude, final double longitude, final LocalDate date) {
        if (date == null) throw new NullPointerException("Date cannot be null");

        if (!weatherServiceEnabled) {
            LOGGER.debug("Weather service disabled, returning default forecast");
            return createDefaultForecast(date);
        }

        try {
            final String url = String.format(
                    java.util.Locale.US,  // Use US locale to ensure dot (.) decimal separator
                    "%s/api/v1/weather/forecast?latitude=%f&longitude=%f&date=%s",
                    weatherServiceUrl,
                    latitude,
                    longitude,
                    date.toString()
            );

            LOGGER.debug("Fetching weather forecast from: {}", url);

            final WeatherForecastResponse response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(WeatherForecastResponse.class);

            if (response == null) {
                LOGGER.warn("Received null response from weather service");
                return createDefaultForecast(date);
            }

            LOGGER.info("Successfully fetched weather forecast for {} at ({}, {})",
                    date, latitude, longitude);

            return new WeatherForecast(
                    response.date(),
                    response.temperatureMax(),
                    response.temperatureMin(),
                    response.precipitationSum(),
                    response.weatherDescription()
            );

        } catch (RestClientException e) {
            LOGGER.error("Failed to fetch weather forecast: {}", e.getMessage());
            return createDefaultForecast(date);
        }
    }

    private WeatherForecast createDefaultForecast(LocalDate date) {
        return new WeatherForecast(
                date,
                20.0,  // Default temp max
                15.0,  // Default temp min
                0.0,   // No precipitation
                "Weather data unavailable"
        );
    }
}