package com.kolaysoft.projecttracking.exception;

public class RiskIssueNotFoundException extends RuntimeException {

    public RiskIssueNotFoundException(Long id) {
        super("Risk veya engel kaydı bulunamadı. Id: " + id);
    }
}
