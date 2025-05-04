package org.springboot.java17.api.order.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Value("${inventory.api.base-url}")
	private String inventoryApiBaseUrl;
	@Bean
	public WebClient webClient(WebClient.Builder builder){
		return builder
				.baseUrl(inventoryApiBaseUrl)
				.build();
	}
}
