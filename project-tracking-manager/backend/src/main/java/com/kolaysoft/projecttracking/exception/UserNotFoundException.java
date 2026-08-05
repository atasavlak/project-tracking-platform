package com.kolaysoft.projecttracking.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(
            Long id
    ) {
        super(
                "Kullanıcı bulunamadı. Kullanıcı ID: " + id
        );
    }
}