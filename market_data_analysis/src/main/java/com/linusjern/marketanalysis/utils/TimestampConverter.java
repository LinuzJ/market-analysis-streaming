package com.linusjern.marketanalysis.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimestampConverter {
    private DateTimeFormatter formatter;

    public TimestampConverter() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss-SSS");
    }

    public long convertToTimestamp(String dateTimeString) throws DateTimeParseException {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
        long timestamp = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        return timestamp;
    }
}