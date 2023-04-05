package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface TaskRepo extends JpaRepository<Task,Long> {
    Optional<Task> findByIdAndOnlineId(Long taskId, String onlineId);

    Set<Task> findAllByPlannerIdAndOnlineId(Long plannerId, String onlineId);
}
