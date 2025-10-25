package dev.slethware.countriesapi.service.country;

import dev.slethware.countriesapi.models.entity.Country;
import dev.slethware.countriesapi.models.response.ApiResponse;

import java.util.List;

public interface CountryService {
    ApiResponse<List<Country>> getAllCountries(String region, String currency, String sort);
    ApiResponse<Country> getCountryByName(String name);
}
