package org.morriswa.taskapp.service;

import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.BadRequestException;
import org.morriswa.taskapp.model.UserProfileRequest;
import org.morriswa.taskapp.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepo userProfileRepo;
    private final Validator validator;

    @Autowired
    UserProfileServiceImpl(
            final UserProfileRepo userProfileRepo,
            final Validator validator) {
        this.userProfileRepo = userProfileRepo;
        this.validator = validator;
    }
    @Override
    public UserProfile getUserProfile(String onlineId) throws BadRequestException {
        return userProfileRepo.findByOnlineId(onlineId)
                .orElseThrow(()->new BadRequestException(String.format(
                        "No user registered with ID: %s",
                        onlineId)));
    }

    @Override
    public void updateUserProfile(@Valid UserProfileRequest request) {
        var userToUpdate = userProfileRepo.findByOnlineId(request.getOnlineId())
                .orElse(UserProfile.builder()
                        .email(request.getEmail())
                        .displayName(request.getDisplayName())
                        .build());

        var profileErrors = validator.validate(userToUpdate);
        if (!profileErrors.isEmpty()) throw new ConstraintViolationException(profileErrors);

        userProfileRepo.save(userToUpdate);
    }

    @Override
    public void deleteUserData(String onlineId) throws BadRequestException {
        var userToDelete = userProfileRepo.findByOnlineId(onlineId)
                .orElseThrow(()->new BadRequestException(String.format(
                        "No user registered with ID: %s",
                        onlineId)));
        userProfileRepo.delete(userToDelete);
    }
}
