package org.example.backend_tunisiahub.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.lieux-dir:../frontend/public/assets/images/lieux/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les images depuis le dossier local via /uploads/lieux/**
        String absolutePath = new java.io.File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/lieux/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}