package dev.slethware.countriesapi.service.country;

import dev.slethware.countriesapi.models.response.CountryResponse;
import dev.slethware.countriesapi.models.response.StatusResponse;

import java.util.List;

public interface CountryService {
    List<CountryResponse> getAllCountries(String region, String currency, String sort);
    CountryResponse getCountryByName(String name);
    String refreshCountries();
    StatusResponse getStatus();
    void deleteCountry(String name);
}