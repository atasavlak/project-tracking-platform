package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatchActionItemRequest {

    @Positive(message = "Sorumlu kullanıcı ID değeri pozitif olmalıdır.")
    private Long responsibleUserId;

    @Size(
            min = 1,
            max = 200,
            message = "Aksiyon başlığı 1 ile 200 karakter arasında olmalıdır."
    )
    private String title;

    @Size(
            min = 1,
            max = 2000,
            message = "Aksiyon açıklaması 1 ile 2000 karakter arasında olmalıdır."
    )
    private String description;

    private ActionItemPriority priority;

    private ActionItemStatus status;

    private LocalDate targetDate;

    private LocalDate completionDate;

    @Size(
            max = 2000,
            message = "Aksiyon notu en fazla 2000 karakter olabilir."
    )
    private String note;

    public PatchActionItemRequest() {
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActionItemPriority getPriority() {
        return priority;
    }

    public void setPriority(ActionItemPriority priority) {
        this.priority = priority;
    }

    public ActionItemStatus getStatus() {
        return status;
    }

    public void setStatus(ActionItemStatus status) {
        this.status = status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
