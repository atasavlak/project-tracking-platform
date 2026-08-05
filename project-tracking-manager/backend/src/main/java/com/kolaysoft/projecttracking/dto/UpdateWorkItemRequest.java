package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdateWorkItemRequest {

    @NotNull(message = "Haftalık rapor ID değeri zorunludur.")
    @Positive(message = "Haftalık rapor ID değeri pozitif olmalıdır.")
    private Long weeklyReportId;

    @NotBlank(message = "İş kalemi başlığı boş bırakılamaz.")
    @Size(
            max = 200,
            message = "İş kalemi başlığı en fazla 200 karakter olabilir."
    )
    private String title;

    @NotNull(message = "İş kalemi durumu zorunludur.")
    private WorkItemStatus status;

    @NotBlank(message = "Sorumlu kişi boş bırakılamaz.")
    @Size(
            max = 150,
            message = "Sorumlu kişi en fazla 150 karakter olabilir."
    )
    private String responsiblePerson;

    @NotBlank(message = "Planlanan iş açıklaması boş bırakılamaz.")
    @Size(
            max = 2000,
            message = "Planlanan iş en fazla 2000 karakter olabilir."
    )
    private String plannedWork;

    @Size(
            max = 2000,
            message = "Tamamlanan iş en fazla 2000 karakter olabilir."
    )
    private String completedWork;

    @Size(
            max = 2000,
            message = "Açıklama en fazla 2000 karakter olabilir."
    )
    private String description;

    public UpdateWorkItemRequest() {
    }

    public Long getWeeklyReportId() {
        return weeklyReportId;
    }

    public void setWeeklyReportId(Long weeklyReportId) {
        this.weeklyReportId = weeklyReportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkItemStatus getStatus() {
        return status;
    }

    public void setStatus(WorkItemStatus status) {
        this.status = status;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public String getPlannedWork() {
        return plannedWork;
    }

    public void setPlannedWork(String plannedWork) {
        this.plannedWork = plannedWork;
    }

    public String getCompletedWork() {
        return completedWork;
    }

    public void setCompletedWork(String completedWork) {
        this.completedWork = completedWork;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}