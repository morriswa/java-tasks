package org.morriswa.taskapp.control;

import org.morriswa.taskapp.security.CustomJWTPreProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CustomWebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private CustomJWTPreProcessor jwt_pp;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Adds JWT filter to all /tasks/dev/ endpoints...
        registry.addInterceptor(jwt_pp).addPathPatterns("/tasks/dev/**");
    }
}
