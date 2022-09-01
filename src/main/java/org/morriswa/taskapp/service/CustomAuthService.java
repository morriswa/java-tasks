package org.morriswa.taskapp.service;

import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.repo.CustomAuth0UserRepo;
import org.morriswa.taskapp.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

import static org.morriswa.taskapp.exception.CustomExceptionSupply.couldNotAuthenticateUserException;

@Service
public class CustomAuthService {
    private final CustomAuth0UserRepo userRepo;
    private final UserProfileRepo profileRepo;


    @Autowired
    public CustomAuthService(CustomAuth0UserRepo userRepo,UserProfileRepo profileRepo) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    public CustomAuth0User loginFlow(Principal principal, String email)
            throws AuthenticationFailedException {
        final String ONLINE_ID = principal.getName();
        return userRepo.findByOnlineIdAndEmail(ONLINE_ID, email)
                .orElseThrow(couldNotAuthenticateUserException(ONLINE_ID, email));

    }

    public CustomAuth0User registerFlow(Principal principal, String email)
            throws RegistrationFailedException, AuthenticationFailedException
    {
        final String ONLINE_ID = principal.getName();

        Optional<CustomAuth0User> existingUserCheck = userRepo.findByOnlineId(ONLINE_ID);

        if (existingUserCheck.isPresent()) {

            if (!userRepo.existsByOnlineIdAndEmail(ONLINE_ID,email)) {
                CustomAuth0User existingUser = existingUserCheck.get();
                existingUser.setEmail(email);
                userRepo.save(existingUser);
                return existingUser;
            }

            throw new RegistrationFailedException(
                    String.format("A user with ID %s is already registered!", ONLINE_ID));
        }

        userRepo.save(CustomAuth0User.builder()
                .onlineId(ONLINE_ID)
                .email(email).build());

        CustomAuth0User newUser = userRepo.findByOnlineIdAndEmail(ONLINE_ID, email)
                .orElseThrow(couldNotAuthenticateUserException(ONLINE_ID,email));

        profileRepo.save(new UserProfile(newUser));

        if (profileRepo.existsByUser(newUser)) {
            return newUser;
        } else {
            userRepo.delete(newUser);
            throw new RegistrationFailedException("Could not create new user.");
        }
    }
}
