package com.olexandrivchenko.btcaddressaggregator.database.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileSystemOutputGateImplTestConfig {
    @Bean
    @Qualifier("baseOutputFolder")
    public String getBitcoinCaller() {
        return "D:\\test_output";
    }
}
