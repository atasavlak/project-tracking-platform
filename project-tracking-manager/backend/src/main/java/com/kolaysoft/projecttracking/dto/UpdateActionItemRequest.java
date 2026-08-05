package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateActionItemRequest {

    @Positive(message = "Sorumlu kullanıcı ID değeri pozitif olmalıdır.")
    private Long responsibleUserId;

    @NotBlank(message = "Aksiyon başlığı boş bırakılamaz.")
    @Size(
            max = 200,
            message = "Aksiyon başlığı en fazla 200 karakter olabilir."
    )
    private String title;

    @NotBlank(message = "Aksiyon açıklaması boş bırakılamaz.")
    @Size(
            max = 2000,
            message = "Aksiyon açıklaması en fazla 2000 karakter olabilir."
    )
    private String description;

    @NotNull(message = "Aksiyon önceliği zorunludur.")
    private ActionItemPriority priority;

    @NotNull(message = "Aksiyon durumu zorunludur.")
    private ActionItemStatus status;

    @NotNull(message = "Hedef tarih zorunludur.")
    private LocalDate targetDate;

    private LocalDate completionDate;

    @Size(
            max = 2000,
            message = "Aksiyon notu en fazla 2000 karakter olabilir."
    )
    private String note;

    public UpdateActionItemRequest() {
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
