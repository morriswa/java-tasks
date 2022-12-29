package org.morriswa.taskapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig

{
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${auth0.audience}")
    private String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    protected DefaultSecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

            .authorizeRequests()
                .antMatchers("/health")
                .permitAll()
                .and()

            .authorizeRequests()
                .anyRequest()
                .authenticated().and()
                .cors().and()
                .oauth2ResourceServer()
                .jwt().and().withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
                    @Override
                    public <O extends BearerTokenAuthenticationFilter> O postProcess(O filter) {
                        filter.setAuthenticationFailureHandler((request, response, exception) -> {
                            Map<String, Object> custom_error_response = new HashMap<>() {{
                                put("error", "jwt rejected");
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
                });


        return http.build();
    }

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
}

