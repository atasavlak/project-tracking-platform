package com.kolaysoft.projecttracking.exception;

public class WorkItemNotFoundException extends RuntimeException {

    public WorkItemNotFoundException(Long id) {
        super("İş kalemi bulunamadı. Id: " + id);
    }
}