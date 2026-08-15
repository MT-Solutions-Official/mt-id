package com.mtsolutions.application.utils;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
public class DateUtils {

    @ConfigProperty(name = "app.mt.id.timezone")
    String timezone;

    public LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of(timezone));
    }

    public LocalDate today() {
        return LocalDate.now(ZoneId.of(timezone));
    }

    public String formatDisplay(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }
}
