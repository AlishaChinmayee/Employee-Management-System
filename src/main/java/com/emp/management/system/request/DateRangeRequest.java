package com.emp.management.system.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateRangeRequest {

    private String startDate; // Accept date strings in "YYYY-MM-DD" format
    private String endDate;   // Accept date strings in "YYYY-MM-DD" format

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getStartDateTime() {
        try {
            if (startDate == null) {
                throw new IllegalArgumentException("Start date is missing. Please provide a valid startDate.");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(startDate, formatter);
            return localDate.atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid startDate format. Please provide a valid date in 'yyyy-MM-dd' format.");
        }
    }

    public LocalDateTime getEndDateTime() {
        try {
            if (endDate == null) {
                throw new IllegalArgumentException("End date is missing. Please provide a valid endDate.");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(endDate, formatter);
            LocalDateTime endDateTime = localDate.atTime(LocalTime.MAX);

            return endDateTime;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid endDate format. Please provide a valid date in 'yyyy-MM-dd' format.");
        }
    }

}
