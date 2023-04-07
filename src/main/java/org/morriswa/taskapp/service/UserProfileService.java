package org.morriswa.taskapp.service;

import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.common.model.BadRequestException;
import org.morriswa.taskapp.model.UserProfileRequest;

public interface UserProfileService {
    UserProfile getUserProfile(String onlineId) throws BadRequestException;
    void updateUserProfile(UserProfileRequest request);
    void deleteUserData(String onlineId) throws BadRequestException;
}
