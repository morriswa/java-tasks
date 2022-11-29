package org.morriswa.taskapp.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.morriswa.taskapp.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@Component @Slf4j
public class CustomJWTPreProcessor extends WebRequestHandlerInterceptorAdapter {
    @Autowired
    private Environment env;

    public CustomJWTPreProcessor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getHeader("Authorization") == null) {
            return super.preHandle(request, response, handler);
        }

        JwkProvider provider = new UrlJwkProvider(Objects.requireNonNull(env.getProperty("auth0.domain")));

        String token = request.getHeader("Authorization").substring(7);

        DecodedJWT verified_jwt;

        try {
            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
                    .build();
            verified_jwt = verifier.verify(jwt);
        }
        catch (Exception e) {
            throw new AuthenticationFailedException(e.getMessage());
        }

        Set<GrantedAuthority> jwt_authorities = new HashSet<>(){{
            var claims = verified_jwt.getClaims().get("scope").toString()
                    .replace("\"", "")
                    .split(" ");

            for (var claim : claims) {
                add((GrantedAuthority) () -> claim);
            }
        }};

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
             public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
             }

             @Override
             public String getName() {
                 return verified_jwt.getSubject();
             }
        });

        return super.preHandle(request, response, handler);
    }

}
