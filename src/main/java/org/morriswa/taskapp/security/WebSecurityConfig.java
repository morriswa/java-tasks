package org.morriswa.taskapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Value("${server.path}")
    private String PATH;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                    .antMatchers(PATH + "/**").permitAll()
                .and()
                .authorizeRequests()
                    .antMatchers("/health").permitAll();
//        http.authorizeRequests().anyRequest().permitAll();
//        http.authorizeRequests(auth -> auth.anyRequest().authenticated())
//        .oauth2ResourceServer().oauth2ResourceServer().jwt();

        return http.build();
    }
}