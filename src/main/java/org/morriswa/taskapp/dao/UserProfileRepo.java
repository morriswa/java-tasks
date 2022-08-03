package org.morriswa.taskapp.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepo extends JpaRepository<UserProfile,Long> {
    public Optional<UserProfile> findByUser(CustomAuth0User user);
}
