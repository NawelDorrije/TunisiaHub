package org.example.backend_tunisiahub.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")                    // Utilise uniquement ceci pour le développement
                .allowedOriginPatterns(
                        "http://localhost:4200",
                        "https://*.ngrok-free.app",
                        "https://*.ngrok-free.dev"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)                        // Garde true si tu utilises JWT ou cookies
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get("uploads");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toAbsolutePath() + "/");
    }
}