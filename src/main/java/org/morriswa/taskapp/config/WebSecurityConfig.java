package org.morriswa.taskapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.starter.model.DefaultErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.GregorianCalendar;

/**
 * MorrisWA Custom Spring Security Configurations.
 * Enables Spring Security running as oauth2 resource server.
 *
 * @author William A. Morris [william@morriswa.org]
 */
@Configuration
@EnableWebSecurity // Enables Spring Security for Web Services importing this config
public class WebSecurityConfig
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${auth0.audience}")
    private String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;
    @Value("${server.path}")
    private String path;
    @Value("${auth0.scope.secureroutes}")
    private String securedRoutesScope;

    @Bean
    protected JwtDecoder jwtDecoder() {
        // all jwts will be decoded with decoder...
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuer); // from User-provided issuer

        // create a new Audience Validator with user-provided audience (should be protected uris)
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(audience);
        // create a new Jwt Validator from User-provided issuer
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        // create a new Jwt Validator
        OAuth2TokenValidator<Jwt> tokenValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        // save newly created Validator
        jwtDecoder.setJwtValidator(tokenValidator);

        return jwtDecoder;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http // All http requests will...
                // Be stateless
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeHttpRequests(authorize -> authorize
                        // Will require authentication for secured routes
                        .mvcMatchers("/" + path + "**").hasAuthority("SCOPE_"+securedRoutesScope)
                        // Will allow any request on /health endpoint
                        .mvcMatchers("/health").permitAll()
                        // Will deny all other unauthenticated requests
                        .anyRequest().denyAll())
                // Will allow cors
                .cors().and()

                // Handle exceptions for Unauthorized requests
                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
                    var customErrorResponse = DefaultErrorResponse.builder()
                            .error("YOU SHALL NOT PASS")
                            .message("The requested service requires authorization, which you didn't bother to provide")
                            .timestamp(new GregorianCalendar())
                            .build();

                    response.getOutputStream().println(
                            objectMapper.writeValueAsString(customErrorResponse));
                    response.setContentType("application/json");
                    response.setStatus(401);
                }).and()

                // Conform toto oauth2 security standards
                .oauth2ResourceServer()
                // provide a jwt
                .jwt().and().withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                    @Override
                    public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                        // Handle exceptions for insufficiently authenticated requests
                        filter.setAuthenticationFailureHandler((request, response, exception) -> {
                            var customErrorResponse = DefaultErrorResponse.builder()
                                    .error("invalid jwt")
                                    .message(exception.getMessage())
                                    .timestamp(new GregorianCalendar())
                                    .build();

                            response.getOutputStream().println(
                                    objectMapper.writeValueAsString(customErrorResponse)
                            );
                            response.setContentType("application/json");

                            BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
                            delegate.commence(request, response, exception);
                        });
                        return filter;
                    }
                })

                // Handle exceptions for forbidden requests
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    var customErrorResponse = DefaultErrorResponse.builder()
                            .error("insufficient scope")
                            .message("The request requires higher privileges than provided by the access token.")
                            .timestamp(new GregorianCalendar())
                            .build();

                    response.setHeader("WWW-Authenticate",
                            "Bearer error='insufficient_scope', error_description='The request requires higher privileges than provided by the access token.', error_uri='https://tools.ietf.org/html/rfc6750#section-3.1'"
                    );
                    response.getOutputStream().println(
                            objectMapper.writeValueAsString(customErrorResponse)
                    );
                    response.setContentType("application/json");
                    response.setStatus(403);
                });

        return http.build();
    }
}