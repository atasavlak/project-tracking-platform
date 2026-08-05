package com.kolaysoft.projecttracking.exception;

public class WeeklyReportNotFoundException extends RuntimeException {

    public WeeklyReportNotFoundException(Long id) {
        super("Haftalık rapor bulunamadı. Id: " + id);
    }
}