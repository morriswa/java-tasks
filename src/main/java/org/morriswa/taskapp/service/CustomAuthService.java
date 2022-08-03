package org.morriswa.taskapp.service;

import org.morriswa.taskapp.dao.CustomAuth0User;
import org.morriswa.taskapp.dao.CustomAuth0UserRepo;
import org.morriswa.taskapp.dao.UserProfile;
import org.morriswa.taskapp.dao.UserProfileRepo;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

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
        return userRepo.findByOnlineIdAndEmail(principal.getName(), email)
                .orElseThrow(() -> new AuthenticationFailedException(
                        String.format("User with ID %s and email %s could not be authenticated.",
                                principal.getName(),
                                email)));

    }

    public CustomAuth0User registerFlow(Principal principal, String email) throws RegistrationFailedException {
        Optional<CustomAuth0User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isPresent()) {
            throw new RegistrationFailedException("A user with that email address is already registered!");
        } else {
            userRepo.save(new CustomAuth0User(principal.getName(),email));
            CustomAuth0User newUser =  userRepo.findByOnlineIdAndEmail(principal.getName(),email)
                    .orElseThrow(() -> new RegistrationFailedException("User could not be registered..."));
            profileRepo.save(new UserProfile(newUser));
            return newUser;
        }

    }
}
