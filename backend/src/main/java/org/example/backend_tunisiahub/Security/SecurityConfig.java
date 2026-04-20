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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                        // ───────────────────────── SWAGGER ─────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ───────────────────────── AUTH (public) ─────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // ───────────────────────── PUBLIC GETS ─────────────────────────
                        // IMPORTANT : ces règles doivent être déclarées UNE SEULE FOIS,
                        // sans doublon plus bas. La première règle qui matche s'applique.
                        .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/spots/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/equipements/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/activities/**").permitAll()

                        // ───────────────────────── USERS ─────────────────────────
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ───────────────────────── CAMPINGS (write) ─────────────
                        // Les GET sont déjà couverts par permitAll ci-dessus
                        .requestMatchers(HttpMethod.POST,   "/api/campings/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/campings/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/campings/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── SPOTS (write) ─────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/spots/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/spots/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/spots/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── ACTIVITIES (write) ────────────
                        .requestMatchers(HttpMethod.POST,   "/api/activities/template").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/activities/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/activities/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/activities/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── EQUIPEMENTS (write) ───────────
                        .requestMatchers(HttpMethod.POST,   "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipements/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── RESERVATIONS ─────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/reservations").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/reservations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/reservations/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/reservations/*/status").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/*/cancel").hasAnyRole("CLIENT", "ADMIN")

                        // ───────────────────────── PAYMENTS ─────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/payments/deposit/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/refund").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/resend").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/payments/scan").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/settle").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/payments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/payments/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── PRICING ──────────────────────
                        .requestMatchers(HttpMethod.GET,  "/api/pricing/**").hasAnyRole("CLIENT", "OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pricing/run").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/pricing/**").hasAnyRole("OWNER", "ADMIN")

                        // ───────────────────────── DEFAULT ──────────────────────
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return (String email) -> {
            User user = userRepository.findByEmail(email);
            if (user == null) throw new UsernameNotFoundException("User not found: " + email);
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
        // En dev : accepte localhost:4200. En prod, remplace par le vrai domaine.
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}