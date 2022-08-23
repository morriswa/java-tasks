package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomAuth0UserRepo extends JpaRepository<CustomAuth0User, Long> {
    public Optional<CustomAuth0User> findByOnlineIdAndEmail(String onlineId, String email);

    public Optional<CustomAuth0User> findByEmail(String email);
}
