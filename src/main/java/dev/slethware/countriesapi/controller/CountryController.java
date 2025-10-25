package dev.slethware.countriesapi.controller;

import dev.slethware.countriesapi.models.entity.Country;
import dev.slethware.countriesapi.models.response.ApiResponse;
import dev.slethware.countriesapi.service.country.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Country>>> getAllCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort) {

        ApiResponse<List<Country>> response = countryService.getAllCountries(region, currency, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<Country>> getCountryByName(@PathVariable String name) {
        ApiResponse<Country> response = countryService.getCountryByName(name);
        return ResponseEntity.ok(response);
    }
}