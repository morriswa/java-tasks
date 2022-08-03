package org.morriswa.taskapp.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.morriswa.taskapp.dao.CustomAuth0User;
import org.morriswa.taskapp.dao.CustomAuth0UserRepo;
import org.morriswa.taskapp.dao.UserProfileRepo;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.service.CustomAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.morriswa.taskapp.TestSuite.getPrincipal;

@SpringBootTest
class CustomAuthServiceTest {
    private final CustomAuthService test;

    private final CustomAuth0UserRepo userRepo = Mockito.mock(CustomAuth0UserRepo.class);


    @Autowired
    public CustomAuthServiceTest() {
        UserProfileRepo profileRepo = Mockito.mock(UserProfileRepo.class);
        this.test = new CustomAuthService(userRepo, profileRepo);
    }

    @Test
    void loginFlowTest() throws AuthenticationFailedException {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";

        when(userRepo.findByOnlineIdAndEmail(p.getName(),email))
                .thenReturn(Optional.of(
                        new CustomAuth0User(1L, p.getName(), email)));

        assertEquals(new CustomAuth0User(1L, p.getName(), email),test.loginFlow(p,email));
    }

    @Test
    void loginFlowShouldFailTest() {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";

        when(userRepo.findByOnlineIdAndEmail(p.getName(),email))
                .thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,() -> test.loginFlow(p,email));
    }

    @Test
    void registrationTest() throws RegistrationFailedException {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);

        when(userRepo.findByEmail(email))
                .thenReturn(Optional.empty());
        when(userRepo.findByOnlineIdAndEmail(p.getName(),email))
                .thenReturn(Optional.of(testUser));

        CustomAuth0User newUser = test.registerFlow(p,email);
        assertEquals(testUser.getOnlineId(),newUser.getOnlineId());
        assertEquals(testUser.getEmail(),newUser.getEmail());
        verify(userRepo).save(testUser);
    }

    @Test
    void registrationShouldFailDuplicateTest() throws RegistrationFailedException {
        Principal p = getPrincipal();
        String email = "junit@morriswa.org";
        CustomAuth0User testUser = new CustomAuth0User(p.getName(),email);

        when(userRepo.findByEmail(email))
                .thenReturn(Optional.of(testUser));

        assertThrows(RegistrationFailedException.class,() -> test.registerFlow(p,email));
    }
}