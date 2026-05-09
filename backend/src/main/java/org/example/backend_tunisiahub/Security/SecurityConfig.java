package org.example.backend_tunisiahub.Security;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
>>>>>>> origin/feature/integrated-app-event
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
<<<<<<< HEAD
=======
import org.springframework.security.core.userdetails.*;
>>>>>>> origin/feature/integrated-app-event
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
<<<<<<< HEAD
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
=======
import org.springframework.web.cors.*;
>>>>>>> origin/feature/integrated-app-event

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

<<<<<<< HEAD
    private final JwtFilter jwtFilter;

    @Value("${app.cors.allowed-origin-patterns:http://localhost:4200,http://127.0.0.1:4200,https://*.ngrok-free.app,https://*.ngrok.io}")
    private String[] allowedOriginPatterns;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uploads/**").permitAll()
                        .requestMatchers("/api/restaurant-tables/**").permitAll()
                        .requestMatchers("/api/reservations/**").permitAll()
                        .requestMatchers("/api/restaurants/recommendations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/checkin", "/checkin-public").permitAll()
                        .requestMatchers("/api/accommodations/getAll").permitAll()
                        .requestMatchers("/api/accommodations/get/**").permitAll()
                        .requestMatchers("/api/reviews/getAll").permitAll()
                        .requestMatchers("/api/reviews/get/**").permitAll()
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        .requestMatchers("/api/reviews/add/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/recommendations").permitAll()
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
                        .requestMatchers(HttpMethod.POST, "/api/reservations/checkin").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/my").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations", "/api/reservations/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/confirm", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/cancel", "PATCH"))
                                .hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/reservations/*/complete", "PATCH"))
                                .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/restaurant-tables", "/api/restaurant-tables/**")
                                .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/restaurant-tables/add").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/restaurant-tables/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/restaurant-tables/delete/**")
                                .hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/add").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/update/**").hasRole("ADMIN")
                        .requestMatchers("/api/accommodations/delete/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
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
        config.setAllowedOriginPatterns(Arrays.stream(allowedOriginPatterns)
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
=======
  private final JwtFilter jwtFilter;
  private final UserRepository userRepository;

  @Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
  private String allowedOriginPatternsProperty;

  // ================= SECURITY =================
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

      .authorizeHttpRequests(auth -> auth

        // PUBLIC
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

        // EXAMPLE
        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

        // SECURED
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

  // ================= PASSWORD ENCODER =================
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // ================= CORS =================
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(resolveAllowedOriginPatterns());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
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
>>>>>>> origin/feature/integrated-app-event
}
