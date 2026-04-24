package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        // Public routes
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/accommodations/getAll").permitAll()
                        .requestMatchers("/api/accommodations/get/**").permitAll()
                        .requestMatchers("/api/reviews/getAll").permitAll()
                        .requestMatchers("/api/reviews/get/**").permitAll()
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        .requestMatchers("/api/reviews/add/**").permitAll()

                        // Souvenir shops/products visibility
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/reviews/owner-summary").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/souvenir-shops/reviews/owner-summary").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/reviews/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/reviews/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/reviews/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/reviews/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/shops/*/orders").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/shops/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/products/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")

                        // Owner/Admin management
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/shops/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/shops/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/shops/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/products/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/products/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/products/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/promotions/**").hasAnyRole("OWNER", "ADMIN")

                        // Orders and order items
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/orders/issues").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/orders/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/orders").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/orders/*/status").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/orders/**").hasRole("CLIENT")

                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/order-items/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/order-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/order-items/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/order-items/**").hasRole("ADMIN")

                        // Payments
                        .requestMatchers(HttpMethod.GET, "/api/souvenir-shops/payments/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/souvenir-shops/payments/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/souvenir-shops/payments/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/souvenir-shops/payments/**").hasRole("ADMIN")

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
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
