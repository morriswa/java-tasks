package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.Planner;
import org.morriswa.taskapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepo extends JpaRepository<Task,Long> {
    Optional<Task> findByPlannerAndId(Planner planner, Long task_id);
    Optional<Task> findByIdAndOnlineId(Long taskId, String onlineId);
}
