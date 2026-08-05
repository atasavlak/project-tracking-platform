package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.WeeklyReport;
import com.kolaysoft.projecttracking.entity.WeeklyReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository
        extends JpaRepository<WeeklyReport, Long> {

    List<WeeklyReport>
    findByActiveTrueOrderByWeekStartDateDesc();

    List<WeeklyReport>
    findByProject_IdAndActiveTrueOrderByWeekStartDateDesc(
            Long projectId
    );

    List<WeeklyReport>
    findByStatusAndActiveTrueOrderByWeekStartDateDesc(
            WeeklyReportStatus status
    );

    List<WeeklyReport>
    findByProject_IdAndStatusAndActiveTrueOrderByWeekStartDateDesc(
            Long projectId,
            WeeklyReportStatus status
    );

    Optional<WeeklyReport>
    findFirstByProject_IdAndActiveTrueOrderByWeekStartDateDesc(
            Long projectId
    );

    boolean existsByProject_IdAndWeekStartDateAndActiveTrue(
            Long projectId,
            LocalDate weekStartDate
    );

    boolean existsByProject_IdAndWeekStartDateAndActiveTrueAndIdNot(
            Long projectId,
            LocalDate weekStartDate,
            Long id
    );

    long countByActiveTrue();

    @Query("""
            SELECT COUNT(wr)
            FROM WeeklyReport wr
            WHERE wr.active = true
              AND wr.project.active = true
            """)
    long countVisibleActiveReports();

    @Query("""
            SELECT wr
            FROM WeeklyReport wr
            WHERE wr.active = true
              AND wr.project.active = true
            ORDER BY wr.weekStartDate DESC
            """)
    List<WeeklyReport>
    findVisibleActiveReportsOrderByWeekStartDateDesc();

    @Query("""
            SELECT wr
            FROM WeeklyReport wr
            WHERE wr.active = true
              AND wr.project.active = true
              AND (
                    :projectId IS NULL
                    OR wr.project.id = :projectId
              )
              AND (
                    :status IS NULL
                    OR wr.status = :status
              )
              AND (
                    :weekStartDate IS NULL
                    OR wr.weekStartDate >= :weekStartDate
              )
              AND (
                    :weekEndDate IS NULL
                    OR wr.weekEndDate <= :weekEndDate
              )
              AND (
                    :projectManagerId IS NULL
                    OR wr.project.projectManager.id = :projectManagerId
              )
            ORDER BY wr.weekStartDate DESC
            """)
    List<WeeklyReport> searchVisibleReports(
            @Param("projectId")
            Long projectId,

            @Param("status")
            WeeklyReportStatus status,

            @Param("weekStartDate")
            LocalDate weekStartDate,

            @Param("weekEndDate")
            LocalDate weekEndDate,

            @Param("projectManagerId")
            Long projectManagerId
    );
}