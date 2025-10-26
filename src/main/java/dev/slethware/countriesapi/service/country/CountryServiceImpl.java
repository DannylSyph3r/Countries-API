package dev.slethware.countriesapi.service.country;

import dev.slethware.countriesapi.exception.ResourceNotFoundException;
import dev.slethware.countriesapi.models.dto.CountryApiResponse;
import dev.slethware.countriesapi.models.dto.CurrencyDto;
import dev.slethware.countriesapi.models.dto.ExchangeRateApiResponse;
import dev.slethware.countriesapi.models.entity.Country;
import dev.slethware.countriesapi.models.response.CountryResponse;
import dev.slethware.countriesapi.models.response.StatusResponse;
import dev.slethware.countriesapi.repository.CountryRepository;
import dev.slethware.countriesapi.service.http.HttpClientService;
import dev.slethware.countriesapi.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final HttpClientService httpClientService;
    private final ImageService imageService;
    private final Random random = new Random();

    @Override
    public List<CountryResponse> getAllCountries(String region, String currency, String sort) {
        log.info("Fetching countries with filters - region: {}, currency: {}, sort: {}",
                region, currency, sort);

        List<Country> countries = countryRepository.findCountriesWithFiltersAndSorting(
                region, currency, sort
        );

        log.info("Found {} countries matching the filters", countries.size());

        return toResponseList(countries);
    }

    @Override
    public CountryResponse getCountryByName(String name) {
        log.info("Fetching country by name: {}", name);

        Country country = countryRepository.findByNameIgnoreCase(name).orElseThrow(() -> {
            log.error("Country not found: {}", name);
            return new ResourceNotFoundException("Country not found: " + name);
        });

        log.info("Successfully found country: {}", country.getName());

        return toResponse(country);
    }

    @Override
    @Transactional
    public String refreshCountries() {
        log.info("Starting country refresh process");

        try {
            // Fetch data from external APIs
            CountryApiResponse[] countryResponses = httpClientService.fetchAllCountries();
            ExchangeRateApiResponse exchangeRateResponse = httpClientService.fetchExchangeRates();
            Map<String, Double> exchangeRates = exchangeRateResponse.getRates();

            log.info("Processing {} countries", countryResponses.length);

            int updatedCount = 0;
            int insertedCount = 0;

            // Process each country
            for (CountryApiResponse countryResponse : countryResponses) {
                try {
                    Country country = processCountry(countryResponse, exchangeRates);

                    Optional<Country> existingCountry = countryRepository.findByNameIgnoreCase(country.getName());

                    if (existingCountry.isPresent()) {
                        // Update existing country
                        Country existing = existingCountry.get();
                        updateCountryFields(existing, country);
                        countryRepository.save(existing);
                        updatedCount++;
                        log.debug("Updated country: {}", existing.getName());
                    } else {
                        // Insert new country
                        countryRepository.save(country);
                        insertedCount++;
                        log.debug("Inserted new country: {}", country.getName());
                    }
                } catch (Exception e) {
                    log.error("Error processing country: {}", countryResponse.getName(), e);
                }
            }

            log.info("Refresh completed. Inserted: {}, Updated: {}", insertedCount, updatedCount);

            // Generate summary image after refresh
            try {
                long totalCountries = countryRepository.count();
                List<Country> topCountries = countryRepository.findTop5ByEstimatedGdpDesc()
                        .stream()
                        .limit(5)
                        .collect(Collectors.toList());

                LocalDateTime lastRefreshedAt = countryRepository.findMaxLastRefreshedAt();

                imageService.generateSummaryImage(topCountries, totalCountries, lastRefreshedAt);
                log.info("Summary image generated successfully");
            } catch (Exception e) {
                log.error("Error generating summary image", e);
            }

            return String.format("Successfully refreshed countries. Inserted: %d, Updated: %d",
                    insertedCount, updatedCount);

        } catch (Exception e) {
            log.error("Error during country refresh", e);
            throw e;
        }
    }

    @Override
    public StatusResponse getStatus() {
        log.info("Fetching system status");

        long totalCountries = countryRepository.count();
        LocalDateTime lastRefreshedAt = countryRepository.findMaxLastRefreshedAt();

        log.info("Total countries: {}, Last refreshed: {}", totalCountries, lastRefreshedAt);

        return new StatusResponse(totalCountries, lastRefreshedAt);
    }

    @Override
    @Transactional
    public void deleteCountry(String name) {
        log.info("Deleting country: {}", name);

        Country country = countryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.error("Country not found for deletion: {}", name);
                    return new ResourceNotFoundException("Country not found: " + name);
                });

        countryRepository.delete(country);
        log.info("Successfully deleted country: {}", name);
    }

    private Country processCountry(CountryApiResponse response, Map<String, Double> exchangeRates) {
        Country country = new Country();

        // Set basic fields
        country.setName(response.getName());
        country.setCapital(response.getCapital());
        country.setRegion(response.getRegion());
        country.setPopulation(response.getPopulation() != null ? response.getPopulation() : 0L);
        country.setFlagUrl(response.getFlag());
        country.setLastRefreshedAt(LocalDateTime.now());

        // Handle currency code
        String currencyCode = extractCurrencyCode(response);
        country.setCurrencyCode(currencyCode);

        // Handle exchange rate and estimated GDP
        if (currencyCode != null) {
            Double exchangeRate = exchangeRates.get(currencyCode);
            country.setExchangeRate(exchangeRate);

            // Calculate estimated GDP
            Double estimatedGdp = calculateEstimatedGdp(country.getPopulation(), exchangeRate);
            country.setEstimatedGdp(estimatedGdp);
        } else {
            // No currency code - set exchange rate to null and GDP to 0
            country.setExchangeRate(null);
            country.setEstimatedGdp(0.0);
        }

        return country;
    }

    private String extractCurrencyCode(CountryApiResponse response) {
        // If currencies array is empty or null, return null
        if (response.getCurrencies() == null || response.getCurrencies().isEmpty()) {
            log.debug("No currencies found for country: {}", response.getName());
            return null;
        }

        // Get first currency code
        CurrencyDto firstCurrency = response.getCurrencies().get(0);
        return firstCurrency.getCode();
    }

    private Double calculateEstimatedGdp(Long population, Double exchangeRate) {
        // If exchange rate is null (currency not found in rates), return null
        if (exchangeRate == null) {
            return null;
        }

        // If population is 0, GDP will be 0
        if (population == null || population == 0) {
            return 0.0;
        }

        // If exchange rate is 0, avoid division by zero
        if (exchangeRate == 0) {
            return null;
        }

        // Generate random multiplier between 1000 and 2000
        double randomMultiplier = 1000 + (random.nextDouble() * 1000);

        // Calculate: population × random(1000–2000) ÷ exchange_rate
        return (population * randomMultiplier) / exchangeRate;
    }

    private void updateCountryFields(Country existing, Country updated) {
        existing.setName(updated.getName());
        existing.setCapital(updated.getCapital());
        existing.setRegion(updated.getRegion());
        existing.setPopulation(updated.getPopulation());
        existing.setCurrencyCode(updated.getCurrencyCode());
        existing.setExchangeRate(updated.getExchangeRate());
        existing.setEstimatedGdp(updated.getEstimatedGdp()); // New random multiplier
        existing.setFlagUrl(updated.getFlagUrl());
        existing.setLastRefreshedAt(updated.getLastRefreshedAt());
    }

    // Mapper methods
    private CountryResponse toResponse(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getName(),
                country.getCapital(),
                country.getRegion(),
                country.getPopulation(),
                country.getCurrencyCode(),
                country.getExchangeRate(),
                country.getEstimatedGdp(),
                country.getFlagUrl(),
                country.getLastRefreshedAt()
        );
    }

    private List<CountryResponse> toResponseList(List<Country> countries) {
        return countries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}