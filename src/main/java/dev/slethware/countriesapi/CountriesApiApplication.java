package dev.slethware.countriesapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				contact = @Contact(
						name = "Slethware",
						email = "danieleakinola@gmail.com"
				),
				description = "OpenAPI documentation for Countries FX API",
				title = "Countries FX API",
				version = "1.0"
		)
)
public class CountriesApiApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(CountriesApiApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(CountriesApiApplication.class, args);
	}

}