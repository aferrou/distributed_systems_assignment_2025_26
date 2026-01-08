package gr.hua.dit.fittrack.core.port;
import gr.hua.dit.fittrack.core.port.impl.dto.WeatherForecast;

import java.time.LocalDate;

/**
 * Port for weather forecast operations.
 */
public interface WeatherPort {

    /**
     * Gets weather forecast for a specific location and date.
     *
     * @param latitude the latitude coordinate
     * @param longitude the longitude coordinate
     * @param date the date to get forecast for
     * @return weather forecast information
     */
    WeatherForecast getForecast(double latitude, double longitude, LocalDate date);
}
