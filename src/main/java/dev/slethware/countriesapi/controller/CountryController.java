package dev.slethware.countriesapi.controller;

import dev.slethware.countriesapi.models.response.CountryResponse;
import dev.slethware.countriesapi.service.country.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@Tag(name = "Countries", description = "API for managing country data with exchange rates")
public class CountryController {

    private final CountryService countryService;

    @PostMapping("/refresh")
    @Operation(summary = "Refresh country data from external APIs")
    public ResponseEntity<Map<String, String>> refreshCountries() {
        String message = countryService.refreshCountries();
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping
    @Operation(summary = "Get all countries with optional filters")
    public ResponseEntity<List<CountryResponse>> getAllCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort) {

        List<CountryResponse> countries = countryService.getAllCountries(region, currency, sort);
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get country by name")
    public ResponseEntity<CountryResponse> getCountryByName(@PathVariable String name) {
        CountryResponse country = countryService.getCountryByName(name);
        return ResponseEntity.ok(country);
    }
}