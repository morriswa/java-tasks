package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface PlannerRepo extends JpaRepository<Planner,Long> {
    Optional<Planner> findByIdAndOnlineId(Long plannerId, String onlineId);
    boolean existsByOnlineIdAndName(String onlineId, String name);
    Set<Planner> findAllByOnlineId(String onlineId);
}
