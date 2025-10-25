package dev.slethware.countriesapi.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryApiResponse {
    private String name;
    private String capital;
    private String region;
    private Long population;
    private List<CurrencyDto> currencies;
    private String flag;
}