package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public routes
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uploads/**").permitAll()
                        .requestMatchers("/api/restaurant-tables/**").permitAll()
                        .requestMatchers("/api/accommodations/getAll").permitAll()
                        .requestMatchers("/api/accommodations/get/**").permitAll()
                        .requestMatchers("/api/reviews/getAll").permitAll()
                        .requestMatchers("/api/reviews/get/**").permitAll()
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        .requestMatchers("/api/reviews/add/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menus", "/api/menus/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-items", "/api/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/menus/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menus/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menus/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/menu-items/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu-items/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reservations", "/api/reservations/**")
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/confirm", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/cancel", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/complete", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/restaurant-tables", "/api/restaurant-tables/**")
                                .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/restaurant-tables/add").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/restaurant-tables/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurant-tables/delete/**")
                                .hasRole("ADMIN")

                        // Admin only
                        .requestMatchers("/api/accommodations/add").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/update/**").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/delete/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Authenticated users
                       // .requestMatchers("/api/reviews/add/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/update/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/delete/**").hasAnyRole("CLIENT", "ADMIN")
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
        // Patterns cover both localhost and 127.0.0.1 (different Origins → CORS 403 if only one is listed).
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
