package com.nikitin.app.data.db.postgres.filler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringToDayTimeFormatter {

    public LocalDateTime formatTimeFromString(String startTime) {
        DateTimeFormatter userFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        return LocalDateTime.of(
                java.time.LocalDate.parse(startTime, userFormatter),
                java.time.LocalTime.MIN
        );
    }

}
