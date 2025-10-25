package dev.slethware.countriesapi.service.country;

import dev.slethware.countriesapi.exception.ResourceNotFoundException;
import dev.slethware.countriesapi.models.entity.Country;
import dev.slethware.countriesapi.models.response.ApiResponse;
import dev.slethware.countriesapi.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public ApiResponse<List<Country>> getAllCountries(String region, String currency, String sort) {
        log.info("Fetching countries with filters - region: {}, currency: {}, sort: {}",
                region, currency, sort);

        List<Country> countries = countryRepository.findCountriesWithFiltersAndSorting(
                region, currency, sort
        );

        log.info("Found {} countries matching the filters", countries.size());

        return ApiResponse.<List<Country>>builder()
                .message("Successfully retrieved countries")
                .statusCode(200)
                .isSuccessful(true)
                .data(countries)
                .build();
    }

    @Override
    public ApiResponse<Country> getCountryByName(String name) {
        log.info("Fetching country by name: {}", name);

        Country country = countryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.error("Country not found: {}", name);
                    return new ResourceNotFoundException("Country not found: " + name);
                });

        log.info("Successfully found country: {}", country.getName());

        return ApiResponse.<Country>builder()
                .message("Successfully retrieved country")
                .statusCode(200)
                .isSuccessful(true)
                .data(country)
                .build();
    }
}