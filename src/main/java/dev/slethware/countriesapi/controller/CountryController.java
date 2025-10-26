package dev.slethware.countriesapi.controller;

import dev.slethware.countriesapi.exception.ResourceNotFoundException;
import dev.slethware.countriesapi.models.response.CountryResponse;
import dev.slethware.countriesapi.models.response.ErrorResponse;
import dev.slethware.countriesapi.models.response.StatusResponse;
import dev.slethware.countriesapi.service.country.CountryService;
import dev.slethware.countriesapi.service.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Countries", description = "API for managing country data with exchange rates")
public class CountryController {

    private final CountryService countryService;
    private final ImageService imageService;

    @PostMapping("/countries/refresh")
    @Operation(summary = "Refresh country data from external APIs")
    public ResponseEntity<Map<String, String>> refreshCountries() {
        String message = countryService.refreshCountries();
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/countries")
    @Operation(summary = "Get all countries with optional filters")
    public ResponseEntity<List<CountryResponse>> getAllCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort) {

        List<CountryResponse> countries = countryService.getAllCountries(region, currency, sort);
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/countries/{name}")
    @Operation(summary = "Get country by name")
    public ResponseEntity<CountryResponse> getCountryByName(@PathVariable String name) {
        CountryResponse country = countryService.getCountryByName(name);
        return ResponseEntity.ok(country);
    }

    @DeleteMapping("/countries/{name}")
    @Operation(summary = "Delete a country by name")
    public ResponseEntity<Void> deleteCountry(@PathVariable String name) {
        countryService.deleteCountry(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/countries/image")
    @Operation(summary = "Get the summary image")
    public ResponseEntity<?> getSummaryImage() {
        try {
            byte[] imageBytes = imageService.getSummaryImage();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (ResourceNotFoundException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .error("Summary image not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get system status")
    public ResponseEntity<StatusResponse> getStatus() {
        StatusResponse status = countryService.getStatus();
        return ResponseEntity.ok(status);
    }
}