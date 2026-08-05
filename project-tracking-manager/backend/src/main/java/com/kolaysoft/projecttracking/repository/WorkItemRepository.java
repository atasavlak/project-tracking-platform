package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.WorkItem;
import com.kolaysoft.projecttracking.entity.WorkItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WorkItemRepository
        extends JpaRepository<WorkItem, Long> {

    List<WorkItem>
    findByActiveTrueOrderByCreatedAtDesc();

    List<WorkItem>
    findByWeeklyReport_IdAndActiveTrueOrderByCreatedAtAsc(
            Long weeklyReportId
    );

    List<WorkItem>
    findByStatusAndActiveTrueOrderByCreatedAtDesc(
            WorkItemStatus status
    );

    List<WorkItem>
    findByWeeklyReport_IdAndStatusAndActiveTrueOrderByCreatedAtAsc(
            Long weeklyReportId,
            WorkItemStatus status
    );

    List<WorkItem>
    findByStatusInAndActiveTrueOrderByCreatedAtDesc(
            Collection<WorkItemStatus> statuses
    );

    boolean existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrue(
            Long weeklyReportId,
            String title
    );

    boolean existsByWeeklyReport_IdAndTitleIgnoreCaseAndActiveTrueAndIdNot(
            Long weeklyReportId,
            String title,
            Long id
    );

    long countByActiveTrue();

    long countByStatusAndActiveTrue(
            WorkItemStatus status
    );

    @Query("""
            SELECT wi
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
            ORDER BY wi.createdAt DESC
            """)
    List<WorkItem> findVisibleActiveWorkItemsOrderByCreatedAtDesc();

    @Query("""
            SELECT wi
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
              AND wi.status = :status
            ORDER BY wi.createdAt DESC
            """)
    List<WorkItem> findVisibleActiveWorkItemsByStatusOrderByCreatedAtDesc(
            @Param("status")
            WorkItemStatus status
    );

    @Query("""
            SELECT wi
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
              AND wi.status IN :statuses
            ORDER BY wi.createdAt DESC
            """)
    List<WorkItem> findVisibleActiveWorkItemsByStatusInOrderByCreatedAtDesc(
            @Param("statuses")
            Collection<WorkItemStatus> statuses
    );

    @Query("""
            SELECT COUNT(wi)
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
            """)
    long countVisibleActiveWorkItems();

    @Query("""
            SELECT COUNT(wi)
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
              AND wi.status = :status
            """)
    long countVisibleActiveWorkItemsByStatus(
            @Param("status")
            WorkItemStatus status
    );

    @Query("""
            SELECT wi
            FROM WorkItem wi
            WHERE wi.active = true
              AND wi.weeklyReport.active = true
              AND wi.weeklyReport.project.active = true
              AND (
                    :projectId IS NULL
                    OR wi.weeklyReport.project.id = :projectId
              )
              AND (
                    :weeklyReportId IS NULL
                    OR wi.weeklyReport.id = :weeklyReportId
              )
              AND (
                    :status IS NULL
                    OR wi.status = :status
              )
              AND (
                    :responsiblePerson IS NULL
                    OR LOWER(wi.responsiblePerson)
                       LIKE CONCAT(
                            CONCAT('%', LOWER(:responsiblePerson)),
                            '%'
                       )
              )
              AND (
                    :projectManagerId IS NULL
                    OR wi.weeklyReport.project.projectManager.id =
                       :projectManagerId
              )
            ORDER BY wi.createdAt DESC
            """)
    List<WorkItem> searchVisibleWorkItems(
            @Param("projectId")
            Long projectId,

            @Param("weeklyReportId")
            Long weeklyReportId,

            @Param("status")
            WorkItemStatus status,

            @Param("responsiblePerson")
            String responsiblePerson,

            @Param("projectManagerId")
            Long projectManagerId
    );
}