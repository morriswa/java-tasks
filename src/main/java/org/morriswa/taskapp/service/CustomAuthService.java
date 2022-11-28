package org.morriswa.taskapp.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.taskapp.entity.CustomAuth0User;
import org.morriswa.taskapp.entity.UserProfile;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.morriswa.taskapp.exception.RegistrationFailedException;
import org.morriswa.taskapp.repo.CustomAuth0UserRepo;
import org.morriswa.taskapp.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.morriswa.taskapp.exception.CustomExceptionSupply.couldNotAuthenticateUserException;

@Service @Slf4j
public class CustomAuthService {
    private final CustomAuth0UserRepo userRepo;
    private final UserProfileRepo profileRepo;
    private final Environment env;


    @Autowired
    public CustomAuthService(CustomAuth0UserRepo userRepo,UserProfileRepo profileRepo, Environment e) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.env = e;
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

    public DecodedJWT verifyJwt(String authorization) throws JwkException, JWTVerificationException {
        JwkProvider provider = new UrlJwkProvider(Objects.requireNonNull(env.getProperty("auth0.domain")));

        String token = authorization.substring(7);

        DecodedJWT jwt = JWT.decode(token);
        // Get the kid from received JWT token
        Jwk jwk = provider.get(jwt.getKeyId());

        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                .build();

        var verified_jwt = verifier.verify(jwt);

        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                Set<GrantedAuthority> authorities = new HashSet<>();
                var claims = verified_jwt.getClaims().get("scope").toString()
                        .replace("\"","")
                        .split(" ");

                for (var claim : claims) {
                    authorities.add((GrantedAuthority) () -> claim);
                }

                return authorities;
            }

            @Override
            public Object getCredentials() {
                return verified_jwt;
            }

            @Override
            public Object getDetails() {
                return verified_jwt.getPayload();
            }

            @Override
            public Object getPrincipal() {
                return (Principal) verified_jwt::getSubject;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return verified_jwt.getSubject();
            }
        });

        return verified_jwt;
    }

    public Boolean requireJwt(String authorization)
            throws JwkException {
        try {
            var jwt = verifyJwt(authorization);
            return Boolean.TRUE;
        } catch (Exception e) {
            throw new AccessDeniedException(e.getMessage());
        }
    }

//    public Boolean requireJwt(String authorization, Set<String> required_scopes)
//            throws JwkException, RequiredScopeMissingException {
//
//
//    }
}
