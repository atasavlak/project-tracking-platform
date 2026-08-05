package com.kolaysoft.projecttracking.dto;

import com.kolaysoft.projecttracking.entity.ActionItemPriority;
import com.kolaysoft.projecttracking.entity.ActionItemStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DashboardOverdueActionResponse {

    private Long actionItemId;
    private Long projectId;
    private String projectName;
    private String title;
    private ActionItemPriority priority;
    private ActionItemStatus status;
    private Long responsibleUserId;
    private String responsibleUserFullName;
    private LocalDate targetDate;
    private long overdueDays;
}
