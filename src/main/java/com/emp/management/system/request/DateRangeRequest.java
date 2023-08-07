package com.emp.management.system.request;

import java.time.LocalDateTime;

public class DateRangeRequest {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public DateRangeRequest() {
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
