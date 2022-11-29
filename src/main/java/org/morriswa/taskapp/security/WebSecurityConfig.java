package org.morriswa.taskapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/health").permitAll();
        http.csrf().disable();
//
        http.authorizeRequests().antMatchers("${server.path}/**").permitAll().and().cors();
//        http.authorizeRequests().anyRequest().permitAll();
//        http.authorizeRequests(auth -> auth.anyRequest().authenticated())
//        .oauth2ResourceServer().oauth2ResourceServer().jwt();

        return http.build();
    }
}