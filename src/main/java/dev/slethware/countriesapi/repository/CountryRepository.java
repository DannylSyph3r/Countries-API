package dev.slethware.countriesapi.repository;

import dev.slethware.countriesapi.models.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query("SELECT c FROM Country c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Country> findByNameIgnoreCase(@Param("name") String name);

    @Query("""
        SELECT c FROM Country c
        WHERE (:region IS NULL OR LOWER(c.region) = LOWER(:region))
        AND (:currencyCode IS NULL OR LOWER(c.currencyCode) = LOWER(:currencyCode))
        ORDER BY
            CASE WHEN :sort = 'gdp_desc' THEN c.estimatedGdp END DESC,
            CASE WHEN :sort = 'gdp_asc' THEN c.estimatedGdp END ASC,
            CASE WHEN :sort = 'name_asc' THEN c.name END ASC,
            CASE WHEN :sort = 'name_desc' THEN c.name END DESC,
            CASE WHEN :sort IS NULL THEN c.id END ASC
    """)
    List<Country> findCountriesWithFiltersAndSorting(
            @Param("region") String region,
            @Param("currencyCode") String currencyCode,
            @Param("sort") String sort
    );

    @Query("SELECT MAX(c.lastRefreshedAt) FROM Country c")
    LocalDateTime findMaxLastRefreshedAt();

    @Query("SELECT c FROM Country c ORDER BY c.estimatedGdp DESC NULLS LAST")
    List<Country> findTop5ByEstimatedGdpDesc();
}