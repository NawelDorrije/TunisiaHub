package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;

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
                        // PUBLIC ENDPOINTS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                  .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // STATIC RESOURCES
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uploads/**").permitAll()

                        // RESTAURANT ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/recommendations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/restaurants/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurants/delete/**").hasRole("ADMIN")

                        // MENU ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/menus", "/api/menus/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/menus/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menus/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menus/delete/**").hasRole("ADMIN")

                        // MENU ITEMS ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/api/restaurants").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/menu-items/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/menu-items/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/menu-items/delete/**").hasRole("ADMIN")

                        // RESTAURANT TABLES ENDPOINTS
                        .requestMatchers("/api/restaurant-tables/**").permitAll()

                        // RESERVATION ENDPOINTS
                        .requestMatchers("/api/reservation-restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservation-restaurants").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/reservation-restaurants/my").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservation-restaurants/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservation-restaurants", "/api/reservation-restaurants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/reservation-restaurants").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservation-restaurants/**").hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservation-restaurants/*/confirm", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservation-restaurants/*/cancel", "PATCH"))
                                .hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservation-restaurants/*/complete", "PATCH"))
                                .hasRole("ADMIN")

                        // ACCOMMODATION ENDPOINTS
                        .requestMatchers("/api/accommodations/getAll").permitAll()
                        .requestMatchers("/api/accommodations/get/**").permitAll()
                        .requestMatchers("/api/accommodations/add").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/update/**").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/delete/**").hasRole("ADMIN")

                        // REVIEW ENDPOINTS
                        .requestMatchers("/api/reviews/getAll").permitAll()
                        .requestMatchers("/api/reviews/get/**").permitAll()
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        .requestMatchers("/api/reviews/add/**").permitAll()
                        .requestMatchers("/api/reviews/update/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/reviews/delete/**").hasAnyRole("CLIENT", "ADMIN")

                        // CHECKIN ENDPOINTS
                        .requestMatchers(HttpMethod.GET, "/checkin", "/checkin-public").permitAll()

                        // USER MANAGEMENT
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // SECURE ALL OTHER ENDPOINTS
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + email);
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getMotDePasse())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(resolveAllowedOriginPatterns());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> resolveAllowedOriginPatterns() {
        return Arrays.stream(allowedOriginPatternsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
