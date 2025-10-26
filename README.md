# Countries FX API

A RESTful API that fetches country data with real-time exchange rates, computes estimated GDP metrics, and provides a caching layer with visual reporting.

## Overview

This API integrates data from [RestCountries](https://restcountries.com) and [ExchangeRate-API](https://www.exchangerate-api.com/) to provide:
- Cached country information with currency exchange rates
- Computed GDP estimates based on population and exchange rates
- Queryable data with filtering and sorting
- Auto-generated visual summary reports

## Tech Stack

- Java 21
- Spring Boot
- MySQL/PostgreSQL
- Maven
- Java Graphics2D (for image generation)

## Prerequisites

### Database Setup

Create a MySQL or PostgreSQL database:

**MySQL/PostgreSQL:**
```sql
CREATE DATABASE countries_api;
```

### Environment Variables

Set the following environment variables:

```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/countries_api
export DATABASE_USERNAME=your_username
export DATABASE_PASSWORD=your_password
```

For PostgreSQL, use:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/countries_api
```

### Database Schema

The application uses Hibernate with `ddl-auto=validate`, so you need to create the schema first, or you can set it to `ddl-auto=update` and let Springboot work its magic:

```sql
CREATE TABLE countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    capital VARCHAR(255),
    region VARCHAR(255),
    population BIGINT NOT NULL,
    currency_code VARCHAR(10),
    exchange_rate DOUBLE,
    estimated_gdp DOUBLE,
    flag_url VARCHAR(500),
    last_refreshed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Getting Started

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the WAR file
java -jar target/countriesapi.war
```

API runs on `http://localhost:8080`

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Endpoints

### 1. Refresh Country Data

**POST** `/countries/refresh`

Fetches all countries from external APIs, calculates exchange rates and GDP, then caches in database. Also generates a summary image.

```bash
curl -X POST http://localhost:8080/countries/refresh
```

**Response:**
```json
{
  "message": "Successfully refreshed countries. Inserted: 250, Updated: 0"
}
```

---

### 2. Get All Countries

**GET** `/countries`

Retrieve all cached countries with optional filtering and sorting.

```bash
# Get all countries
curl http://localhost:8080/countries

# Filter by region
curl "http://localhost:8080/countries?region=Africa"

# Filter by currency
curl "http://localhost:8080/countries?currency=NGN"

# Sort by GDP (descending)
curl "http://localhost:8080/countries?sort=gdp_desc"

# Combine filters
curl "http://localhost:8080/countries?region=Europe&sort=gdp_desc"
```

**Query Parameters:**
- `region` - Filter by geographic region (e.g., Africa, Europe, Asia)
- `currency` - Filter by currency code (e.g., USD, EUR, NGN)
- `sort` - Sort results: `gdp_desc`, `gdp_asc`, `name_asc`, `name_desc`

**Response:**
```json
[
  {
    "id": 1,
    "name": "Nigeria",
    "capital": "Abuja",
    "region": "Africa",
    "population": 206139589,
    "currency_code": "NGN",
    "exchange_rate": 1600.23,
    "estimated_gdp": 25767448125.2,
    "flag_url": "https://flagcdn.com/ng.svg",
    "last_refreshed_at": "2025-10-26T15:55:14.449025"
  }
]
```

---

### 3. Get Country by Name

**GET** `/countries/{name}`

Retrieve a specific country by name (case-insensitive).

```bash
curl http://localhost:8080/countries/Nigeria
curl http://localhost:8080/countries/GHANA
```

**Response:**
```json
{
  "id": 1,
  "name": "Nigeria",
  "capital": "Abuja",
  "region": "Africa",
  "population": 206139589,
  "currency_code": "NGN",
  "exchange_rate": 1600.23,
  "estimated_gdp": 25767448125.2,
  "flag_url": "https://flagcdn.com/ng.svg",
  "last_refreshed_at": "2025-10-26T15:55:14.449025"
}
```

**Error Response (404):**
```json
{
  "error": "Country not found"
}
```

---

### 4. Delete Country

**DELETE** `/countries/{name}`

Delete a country record by name (case-insensitive).

```bash
curl -X DELETE http://localhost:8080/countries/Nigeria
```

**Response:** `204 No Content`

**Error Response (404):**
```json
{
  "error": "Country not found"
}
```

---

### 5. System Status

**GET** `/status`

Get total cached countries and last refresh timestamp.

```bash
curl http://localhost:8080/status
```

**Response:**
```json
{
  "total_countries": 250,
  "last_refreshed_at": "2025-10-26T15:55:14.449025"
}
```

---

### 6. Summary Image

**GET** `/countries/image`

Retrieve the auto-generated visual summary (PNG format). Image is created automatically after each refresh.

```bash
# View in browser
http://localhost:8080/countries/image

# Download
curl http://localhost:8080/countries/image --output summary.png
```

**Image Contains:**
- Total number of countries
- Top 5 countries by estimated GDP
- Last refresh timestamp

**Error Response (404):**
```json
{
  "error": "Summary image not found"
}
```

## How It Works

### Data Flow

1. **Refresh Triggered** → POST `/countries/refresh`
2. **Fetch Countries** → Calls RestCountries API
3. **Fetch Exchange Rates** → Calls ExchangeRate-API
4. **Calculate GDP** → For each country: `population × random(1000-2000) ÷ exchange_rate`
5. **Cache in Database** → Insert new or update existing (by name, case-insensitive)
6. **Generate Image** → Creates visual summary at `cache/summary.png`

### GDP Calculation

```
estimated_gdp = population × random_multiplier(1000-2000) ÷ exchange_rate
```

- Random multiplier is regenerated on each refresh
- If currency not found in rates: `exchange_rate = null`, `estimated_gdp = null`
- If no currency: `exchange_rate = null`, `estimated_gdp = 0`

### Special Handling

- **Multiple Currencies**: Only first currency code is stored
- **No Currency**: Country stored with `currency_code = null`, `estimated_gdp = 0`
- **Currency Not in Rates**: Country stored with `exchange_rate = null`, `estimated_gdp = null`
- **Zero Population**: Results in `estimated_gdp = 0`