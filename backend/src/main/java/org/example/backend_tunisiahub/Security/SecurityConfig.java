package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
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
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // ================= SWAGGER =================
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // ================= AUTH =================
                .requestMatchers("/api/auth/**").permitAll()

                // ================= PUBLIC GETS =================
                .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/campings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/spots/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/equipements/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/activities/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/lieux/**").permitAll()

                // ================= USERS =================
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // ================= ACCOMMODATION RESERVATIONS =================
                .requestMatchers("/api/accommodation-reservations/**")
                    .hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers("/api/accommodation-reservations/statistics")
                    .hasRole("ADMIN")

                // ================= REVIEWS / FEEDBACK =================
                .requestMatchers("/api/reviews/update/**", "/api/reviews/delete/**")
                    .hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers("/api/feedback/**")
                    .hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers("/api/feedback/accommodation/**").permitAll()

                // ================= CAMPINGS =================
                .requestMatchers(HttpMethod.POST, "/api/campings/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/campings/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/campings/**").hasAnyRole("OWNER", "ADMIN")

                // ================= SPOTS =================
                .requestMatchers(HttpMethod.GET, "/api/spots/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/spots/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/spots/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/spots/**").hasAnyRole("OWNER", "ADMIN")

                // ================= ACTIVITIES =================
                .requestMatchers(HttpMethod.POST, "/api/activities/template").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/activities/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/activities/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/activities/**").hasAnyRole("OWNER", "ADMIN")

                // ================= EQUIPMENTS =================
                .requestMatchers(HttpMethod.POST, "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")

                // ================= RESERVATIONS =================
                .requestMatchers(HttpMethod.POST, "/api/reservations").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/status").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/*/cancel").hasAnyRole("CLIENT", "ADMIN")

                // ================= PAYMENTS =================
                .requestMatchers(HttpMethod.POST, "/api/payments/stripe/**").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/payments/stripe/**").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments/deposit/**").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments/*/refund").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments/*/resend").hasAnyRole("CLIENT", "ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("OWNER", "ADMIN")

                // ================= PRICING =================
                .requestMatchers(HttpMethod.GET, "/api/pricing/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/pricing/run").hasRole("ADMIN")

                // ================= DEFAULT =================
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    // ================= USER DETAILS =================
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email);
            if (user == null)
                throw new UsernameNotFoundException("User not found: " + email);

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getMotDePasse())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    // ================= PASSWORD ENCODER =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= CORS =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://192.168.0.*:4200",
                "https://*.ngrok-free.dev",
                "https://*.ngrok-free.app",
                "https://*.ngrok.io"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
