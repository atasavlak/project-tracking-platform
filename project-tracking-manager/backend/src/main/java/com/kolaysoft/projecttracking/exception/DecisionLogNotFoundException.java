package com.kolaysoft.projecttracking.exception;

public class DecisionLogNotFoundException extends RuntimeException {

    public DecisionLogNotFoundException(
            Long id
    ) {
        super(
                "Karar kaydı bulunamadı. ID: "
                        + id
        );
    }
}
