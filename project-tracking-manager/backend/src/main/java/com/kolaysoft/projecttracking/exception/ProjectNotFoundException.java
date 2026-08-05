package com.kolaysoft.projecttracking.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long id) {
        super("Project bulunamadı. Id: " + id);
    }
}