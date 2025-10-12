package com.healthcoin.java_eth_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

// Notate Spring that this class is for configuration.
@Configuration
public class Web3jConfig {

    // Read 'alchemy.api-url' value from application.properties and inject this var 'alchemyApiUrl'.
    @Value("${alchemy.api-url}")
    private String alchemyApiUrl;

    // Register the object returned by this method as a Bean.
    @Bean
    public Web3j web3j() {
        // Generate and return the Web3j object using injected URL.
        return Web3j.build(new HttpService(alchemyApiUrl));
    }
}
