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
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

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
}
