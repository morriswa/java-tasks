package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface PlannerRepo extends JpaRepository<Planner,Long> {

    Optional<Planner> findByUserAndName(CustomAuth0User user, String planner_name);
    Optional<Planner> findByUserAndId(CustomAuth0User user, Long planner_id);

    boolean existsByUserAndName(CustomAuth0User user, String planner_name);

    Set<Planner> findAllByUser(CustomAuth0User user);
}
