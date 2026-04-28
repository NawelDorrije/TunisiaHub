package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOriginPatternsProperty;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/accommodations/getAll").permitAll()
                        .requestMatchers("/api/accommodations/get/**").permitAll()
                        .requestMatchers("/api/reviews/getAll").permitAll()
                        .requestMatchers("/api/reviews/get/**").permitAll()
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        .requestMatchers("/api/reviews/add/**").permitAll()


                        // Admin only
                        .requestMatchers("/api/accommodations/add").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/update/**").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/delete/**").hasRole("ADMIN")
                        //Temporaire
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Authenticated users
                        //.requestMatchers("/api/reviews/add/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/update/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/delete/**").hasAnyRole("CLIENT", "ADMIN")

                        // Swagger (OBLIGATOIRE)
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        // Events (IMPORTANT)
                        .requestMatchers(HttpMethod.GET, "/event/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/share/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/events/upload-image").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/events").permitAll()
                        .requestMatchers(HttpMethod.POST, "/event/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/event/*/publish-facebook").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/event/update").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/event/delete/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/events/recommendation").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/reservations/reserve").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/reservations/create-pending").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/reservations/confirm/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/user/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyRole("ADMIN", "CLIENT")
                        .requestMatchers("/payment/**").permitAll()
                        .requestMatchers("/stripe/**").permitAll()
                        .requestMatchers("/email/**").permitAll()
                        .requestMatchers("/weather/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/weather/**").permitAll()
                        .requestMatchers("/ai/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/share/**").permitAll()

                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(resolveAllowedOriginPatterns());

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // 🔥 IMPORTANT
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(resolveAllowedOriginPatterns().toArray(new String[0]))
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    private List<String> resolveAllowedOriginPatterns() {
        return Arrays.stream(allowedOriginPatternsProperty.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }
}
