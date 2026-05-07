package org.example.backend_tunisiahub.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    String path = request.getRequestURI();

    if (authHeader != null && authHeader.startsWith("Bearer ")) {

      String token = authHeader.substring(7);

      if (jwtUtil.isTokenValid(token)) {

        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        User user = userRepository.findByEmail(email);

        if (user != null) {

          // inject custom headers
          MutableHttpServletRequest mutableRequest =
            new MutableHttpServletRequest(request);

          mutableRequest.putHeader("X-USER-ID", String.valueOf(user.getId()));
          mutableRequest.putHeader("X-ROLE", role);

          UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
              email,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

          SecurityContextHolder.getContext().setAuthentication(authentication);

          filterChain.doFilter(mutableRequest, response);
          return;
        }
      }
    } else {
      logger.debug("No JWT token provided for path={}", path);
    }

    filterChain.doFilter(request, response);
  }
}
