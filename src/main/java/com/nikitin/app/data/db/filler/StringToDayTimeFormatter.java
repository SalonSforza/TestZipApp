package com.nikitin.app.data.db.filler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringToDayTimeFormatter {

    public LocalDateTime formatTimeOfStartFromString(String startTime) {
        DateTimeFormatter userFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        return LocalDateTime.of(
                java.time.LocalDate.parse(startTime, userFormatter),
                java.time.LocalTime.MIN
        );
    }

    public LocalDateTime formatTimeOfEndFromString(String endTime) {
        DateTimeFormatter userFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return LocalDateTime.of(java.time.LocalDate.parse(endTime, userFormatter),
                LocalTime.MAX);
    }
}
