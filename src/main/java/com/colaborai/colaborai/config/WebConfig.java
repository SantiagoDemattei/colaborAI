package com.colaborai.colaborai.config;

import com.colaborai.colaborai.security.interceptor.OwnershipInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    private final OwnershipInterceptor ownershipInterceptor;

    public WebConfig(OwnershipInterceptor ownershipInterceptor) {
        this.ownershipInterceptor = ownershipInterceptor;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // para todos los endpoints
                        .allowedOrigins("http://localhost:3000") // origen frontend React
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(ownershipInterceptor)
                        .addPathPatterns("/api/**") // Solo aplicar a endpoints de API
                        .excludePathPatterns("/api/auth/**"); // Excluir endpoints de autenticación
            }
        };
    }
}