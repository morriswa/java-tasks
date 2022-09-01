package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepo extends JpaRepository<UserProfile,Long> {
    public Optional<UserProfile> findByUser(CustomAuth0User user);

    boolean existsByUser(CustomAuth0User newUser);
}
