package com.grummans.noyblog.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Application configuration for timezone settings
 * Sets the default timezone for the entire application to UTC
 * This ensures consistency with the database timezone
 */
@Configuration
public class TimezoneConfig {

    @PostConstruct
    public void init() {
        // Set default timezone to UTC for the entire JVM
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("Application timezone set to: " + TimeZone.getDefault().getID());
    }
}

