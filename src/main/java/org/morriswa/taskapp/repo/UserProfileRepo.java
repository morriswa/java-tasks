package org.morriswa.taskapp.repo;

import org.morriswa.taskapp.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepo extends JpaRepository<UserProfile,String> {
    Optional<UserProfile> findByOnlineId(String onlineId);
}
