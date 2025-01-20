package io.hahnsoftware.emp.config;

import io.hahnsoftware.emp.security.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    public WebMvcConfig(UserContextInterceptor userContextInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}