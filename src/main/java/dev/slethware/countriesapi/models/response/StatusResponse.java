package dev.slethware.countriesapi.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StatusResponse(
        @JsonProperty("total_countries") long totalCountries,
        @JsonProperty("last_refreshed_at") LocalDateTime lastRefreshedAt
) {
}