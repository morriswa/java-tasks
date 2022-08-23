package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepo extends JpaRepository<Task,Long> {
    Task getByPlannerAndId(Planner planner, Long task_id);

    Optional<Task> findByPlannerAndId(Planner planner, Long task_id);
}
