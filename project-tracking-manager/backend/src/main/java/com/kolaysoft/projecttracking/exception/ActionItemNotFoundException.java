package com.kolaysoft.projecttracking.exception;

public class ActionItemNotFoundException extends RuntimeException {

    public ActionItemNotFoundException(Long id) {
        super("Aksiyon kaydı bulunamadı. ID: " + id);
    }
}
