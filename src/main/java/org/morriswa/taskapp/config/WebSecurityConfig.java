package org.morriswa.taskapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${auth0.audience}")
    private String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;
    @Value("${server.path}")
    private String path;


    @Bean
    protected JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

            .authorizeHttpRequests(authorize -> authorize
//                    .anyRequest().authenticated()
                    .mvcMatchers("/" + path + "**").hasAuthority("SCOPE_read:profile")
                    .mvcMatchers("/health").permitAll()
                    .anyRequest().denyAll())
                // Exception Handling for Unauthorized requests
                .exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
                    Map<String, Object> custom_error_response = new HashMap<>() {{
                        put("error", "YOU SHALL NOT PASS");
                        put("message", "The requested service requires authorization, which you didn't bother to provide");
                        put("timestamp", new GregorianCalendar().toZonedDateTime().toString());
                    }};

                    response.getOutputStream().println(
                            objectMapper.writeValueAsString(custom_error_response)
                    );
                    response.setContentType("application/json");
                    response.setStatus(401);
                }).and()

            .oauth2ResourceServer()
            .jwt().and().withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                @Override
                public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                    // Exception Handling for insufficiently authenticated requests
                    filter.setAuthenticationFailureHandler((request, response, exception) -> {
                        Map<String, Object> custom_error_response = new HashMap<>() {{
                            put("error", "invalid jwt");
                            put("message", exception.getMessage());
                            put("timestamp", new GregorianCalendar().toZonedDateTime().toString());
                        }};

                        response.getOutputStream().println(
                                objectMapper.writeValueAsString(custom_error_response)
                        );
                        response.setContentType("application/json");

                        BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
                        delegate.commence(request, response, exception);
                    });
                    return filter;
                }
            })
            // Exception Handling for forbidden requests
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                Map<String, Object> custom_error_response = new HashMap<>() {{
                    put("error", "insufficient scope");
                    put("message", "The request requires higher privileges than provided by the access token.");
                    put("timestamp", new GregorianCalendar().toZonedDateTime().toString());
                }};

                response.setHeader("WWW-Authenticate",
                        "Bearer error='insufficient_scope', error_description='The request requires higher privileges than provided by the access token.', error_uri='https://tools.ietf.org/html/rfc6750#section-3.1'"
                       );
                response.getOutputStream().println(
                        objectMapper.writeValueAsString(custom_error_response)
                );
                response.setContentType("application/json");
                response.setStatus(403);
            });


        return http.build();
    }


}

