package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.DecisionStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatchDecisionLogRequest {

    @Positive(message = "Karar sahibi kullanıcı ID değeri pozitif olmalıdır.")
    private Long decisionOwnerId;

    @Size(
            min = 1,
            max = 200,
            message = "Karar başlığı 1 ile 200 karakter arasında olmalıdır."
    )
    private String title;

    @Size(
            min = 1,
            max = 2000,
            message = "Karar açıklaması 1 ile 2000 karakter arasında olmalıdır."
    )
    private String description;

    private LocalDate decisionDate;

    private DecisionStatus status;

    @Size(
            max = 2000,
            message = "Karar notu en fazla 2000 karakter olabilir."
    )
    private String note;

    public PatchDecisionLogRequest() {
    }

    public Long getDecisionOwnerId() {
        return decisionOwnerId;
    }

    public void setDecisionOwnerId(
            Long decisionOwnerId
    ) {
        this.decisionOwnerId = decisionOwnerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(
            LocalDate decisionDate
    ) {
        this.decisionDate = decisionDate;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(
            DecisionStatus status
    ) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(
            String note
    ) {
        this.note = note;
    }
}
