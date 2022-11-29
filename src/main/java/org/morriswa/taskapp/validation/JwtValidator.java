package org.morriswa.taskapp.validation;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JwtValidator implements ConstraintValidator<VerifyJWT,String> {
//    @Autowired
//    private CustomAuthService auth;
    @Autowired
    private Environment env;

    private Set<String> scopes;

    @Override
    public void initialize(VerifyJWT constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        scopes = Arrays.stream(constraintAnnotation.scopes()).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String authorization_header, ConstraintValidatorContext constraintValidatorContext) {
//        try {
//            return auth.requireJwt(token, scopes);
//        } catch (RequiredScopeMissingException e) {
//            constraintValidatorContext
//                    .buildConstraintViolationWithTemplate(e.getMessage())
//                    .addPropertyNode("error").addBeanNode().inIterable().atKey("403")
//                    .addConstraintViolation();
//            return false;
//        } catch (Exception e) {
//            constraintValidatorContext
//                    .buildConstraintViolationWithTemplate(e.getMessage())
//                    .addPropertyNode("error").addBeanNode().inIterable().atKey("401")
//                    .addConstraintViolation();
//            return false;
//        }

        JwkProvider provider = new UrlJwkProvider(Objects.requireNonNull(env.getProperty("auth0.domain")));

        String token = authorization_header.substring(7);

        DecodedJWT verified_jwt;
        try {
            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                    .build();
            verified_jwt = verifier.verify(jwt);
        } catch (Exception e) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(e.getMessage())
                    .addPropertyNode(e.getClass().getSimpleName()).addBeanNode().inIterable().atKey("401")
                    .addConstraintViolation();
            return false;
        }

        List<String> required_scopes = new ArrayList<>(scopes);

        Set<GrantedAuthority> jwt_authorities = new HashSet<>(){{
            var claims = verified_jwt.getClaims().get("scope").toString()
                    .replace("\"", "")
                    .split(" ");

            for (var claim : claims) {
                add((GrantedAuthority) () -> claim);
            }
        }};

        for (var granted : jwt_authorities) {
            required_scopes.remove(granted.getAuthority());
        }

        if (required_scopes.isEmpty()) {
            SecurityContextHolder.getContext().setAuthentication(new Authentication() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return jwt_authorities;
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
                public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}

                @Override
                public String getName() {
                    return verified_jwt.getSubject();
                }
            });

            return true;
        } else {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(
                            String.format("Could not locate authority %s in jwt...",required_scopes))
                    .addPropertyNode("error").addBeanNode().inIterable().atKey("403")
                    .addConstraintViolation();
            return false;
        }


    }

}
