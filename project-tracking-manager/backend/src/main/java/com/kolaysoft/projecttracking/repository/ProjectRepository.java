package com.kolaysoft.projecttracking.repository;

import com.kolaysoft.projecttracking.entity.Project;
import com.kolaysoft.projecttracking.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository
        extends JpaRepository<Project, Long> {

    List<Project> findByActiveTrue();

    List<Project> findByStatusAndActiveTrue(
            ProjectStatus status
    );

    List<Project> findByProjectManager_IdAndActiveTrue(
            Long projectManagerId
    );

    List<Project> findByProjectManager_IdAndStatusAndActiveTrue(
            Long projectManagerId,
            ProjectStatus status
    );

    boolean existsByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );

    long countByActiveTrue();

    long countByStatusAndActiveTrue(
            ProjectStatus status
    );
}