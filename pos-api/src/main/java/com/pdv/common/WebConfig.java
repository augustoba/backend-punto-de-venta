package com.pdv.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${pos.cors-origins}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/api/**").allowedOrigins(origins.split(",")).allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
    }

    /** Errores de negocio (400) y recursos inexistentes (404) con cuerpo {"error": "..."}. */
    @RestControllerAdvice
    public static class Errors {
        @ExceptionHandler(BusinessException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public Map<String, String> business(BusinessException e) { return Map.of("error", e.getMessage()); }

        @ExceptionHandler(NotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public Map<String, String> notFound(NotFoundException e) { return Map.of("error", e.getMessage()); }
    }
}
