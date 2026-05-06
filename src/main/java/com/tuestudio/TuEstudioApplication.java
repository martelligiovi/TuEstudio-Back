package com.tuestudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TuEstudioApplication {
    public static void main(String[] args) {
        SpringApplication.run(TuEstudioApplication.class, args);
    }
}
