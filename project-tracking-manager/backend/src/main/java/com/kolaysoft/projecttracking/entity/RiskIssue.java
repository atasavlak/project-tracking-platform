package com.kolaysoft.projecttracking.entity;

import com.kolaysoft.projecttracking.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_issues")
public class RiskIssue {

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

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "responsible_user_id",
            nullable = false
    )
    private AppUser responsibleUser;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private RiskIssueType type;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            nullable = false,
            length = 2000
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private RiskIssueSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private RiskIssueStatus status;

    @Column(
            name = "follow_up_date",
            nullable = false
    )
    private LocalDate followUpDate;

    @Column(
            name = "resolution_note",
            length = 2000
    )
    private String resolutionNote;

    @Column(nullable = false)
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

    public RiskIssue() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
        active = true;

        if (status == null) {
            status =
                    RiskIssueStatus.OPEN;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt =
                LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public WeeklyReport getWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(
            WeeklyReport weeklyReport
    ) {
        this.weeklyReport =
                weeklyReport;
    }

    public AppUser getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(
            AppUser responsibleUser
    ) {
        this.responsibleUser =
                responsibleUser;
    }

    public RiskIssueType getType() {
        return type;
    }

    public void setType(
            RiskIssueType type
    ) {
        this.type =
                type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title
    ) {
        this.title =
                title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description =
                description;
    }

    public RiskIssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(
            RiskIssueSeverity severity
    ) {
        this.severity =
                severity;
    }

    public RiskIssueStatus getStatus() {
        return status;
    }

    public void setStatus(
            RiskIssueStatus status
    ) {
        this.status =
                status;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(
            LocalDate followUpDate
    ) {
        this.followUpDate =
                followUpDate;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(
            String resolutionNote
    ) {
        this.resolutionNote =
                resolutionNote;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active
    ) {
        this.active =
                active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
