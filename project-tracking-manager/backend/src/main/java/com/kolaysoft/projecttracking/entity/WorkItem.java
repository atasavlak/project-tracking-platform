package com.kolaysoft.projecttracking.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_items")
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "weekly_report_id",
            nullable = false
    )
    private WeeklyReport weeklyReport;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private WorkItemStatus status;

    @Column(
            name = "responsible_person",
            nullable = false,
            length = 150
    )
    private String responsiblePerson;

    @Column(
            name = "planned_work",
            nullable = false,
            length = 2000
    )
    private String plannedWork;

    @Column(
            name = "completed_work",
            length = 2000
    )
    private String completedWork;

    @Column(
            length = 2000
    )
    private String description;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public WorkItem() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
        active = true;

        if (status == null) {
            status = WorkItemStatus.PLANNED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}