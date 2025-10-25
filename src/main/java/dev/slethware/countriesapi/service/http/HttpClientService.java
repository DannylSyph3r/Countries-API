package dev.slethware.countriesapi.service.http;

import dev.slethware.countriesapi.exception.ServiceUnavailableException;
import dev.slethware.countriesapi.models.dto.CountryApiResponse;
import dev.slethware.countriesapi.models.dto.ExchangeRateApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpClientService {

    private final RestTemplate restTemplate;

    @Value("${api.countries.url}")
    private String countriesApiUrl;

    @Value("${api.exchange-rate.url}")
    private String exchangeRateApiUrl;

    public CountryApiResponse[] fetchAllCountries() {
        try {
            log.info("Fetching countries from: {}", countriesApiUrl);

            ResponseEntity<CountryApiResponse[]> response = restTemplate.exchange(
                    countriesApiUrl,
                    HttpMethod.GET,
                    null,
                    CountryApiResponse[].class
            );

            if (response.getBody() == null) {
                throw new ServiceUnavailableException(
                        "External data source unavailable",
                        "Could not fetch data from Countries API"
                );
            }

            log.info("Successfully fetched {} countries", response.getBody().length);
            return response.getBody();

        } catch (Exception e) {
            log.error("Error fetching countries: {}", e.getMessage(), e);
            throw new ServiceUnavailableException(
                    "External data source unavailable",
                    "Could not fetch data from Countries API"
            );
        }
    }

    public ExchangeRateApiResponse fetchExchangeRates() {
        try {
            log.info("Fetching exchange rates from: {}", exchangeRateApiUrl);

            ResponseEntity<ExchangeRateApiResponse> response = restTemplate.exchange(
                    exchangeRateApiUrl,
                    HttpMethod.GET,
                    null,
                    ExchangeRateApiResponse.class
            );

            if (response.getBody() == null || !"success".equalsIgnoreCase(response.getBody().getResult())) {
                throw new ServiceUnavailableException(
                        "External data source unavailable",
                        "Could not fetch data from Exchange Rate API"
                );
            }

            log.info("Successfully fetched exchange rates for {} currencies",
                    response.getBody().getRates().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error fetching exchange rates: {}", e.getMessage(), e);
            throw new ServiceUnavailableException(
                    "External data source unavailable",
                    "Could not fetch data from Exchange Rate API"
            );
        }
    }
}