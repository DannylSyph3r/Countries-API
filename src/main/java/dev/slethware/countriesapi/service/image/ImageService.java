package dev.slethware.countriesapi.service.image;

import dev.slethware.countriesapi.models.entity.Country;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageService {
    void generateSummaryImage(List<Country> topCountries, long totalCountries, LocalDateTime lastRefreshedAt);
    byte[] getSummaryImage();
}