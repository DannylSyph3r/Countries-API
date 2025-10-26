package dev.slethware.countriesapi.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CountryResponse(
        Long id,
        String name,
        String capital,
        String region,
        Long population,
        @JsonProperty("currency_code") String currencyCode,
        @JsonProperty("exchange_rate") Double exchangeRate,
        @JsonProperty("estimated_gdp") Double estimatedGdp,
        @JsonProperty("flag_url") String flagUrl,
        @JsonProperty("last_refreshed_at") LocalDateTime lastRefreshedAt
) {
}