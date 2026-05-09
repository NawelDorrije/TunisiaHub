package org.example.backend_tunisiahub.Controllers.User;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Security.AuthResponse;
import org.example.backend_tunisiahub.Security.JwtUtil;
import org.example.backend_tunisiahub.Security.LoginRequest;
import org.example.backend_tunisiahub.Security.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;

  // ================= REGISTER =================
  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

    if (userRepository.findByEmail(request.getEmail()) != null) {
      return ResponseEntity.badRequest().body("Email already exists");
    }

    RoleUser role = resolveRegistrationRole(request.getRole());

    User user = new User();
    user.setNom(request.getNom());
    user.setPrenom(request.getPrenom());
    user.setEmail(request.getEmail());
    user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
    user.setRole(role);

    User savedUser = userRepository.save(user);

    String token = jwtUtil.generateToken(
      savedUser.getEmail(),
      savedUser.getRole().name(),
      savedUser.getId()
    );

    return ResponseEntity.ok(new AuthResponse(
      token,
      savedUser.getRole().name(),
      savedUser.getEmail(),
      savedUser.getNom(),
      savedUser.getPrenom(),
      savedUser.getId()
    ));
  }

  // ================= LOGIN =================
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail());

    if (user == null) {
      return ResponseEntity.status(404).body("User not found");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getMotDePasse())) {
      return ResponseEntity.status(401).body("Invalid password");
    }

    String token = jwtUtil.generateToken(
      user.getEmail(),
      user.getRole().name(),
      user.getId()
    );

    return ResponseEntity.ok(new AuthResponse(
      token,
      user.getRole().name(),
      user.getEmail(),
      user.getNom(),
      user.getPrenom(),
      user.getId()
    ));
  }

  // ================= ROLE HELPER =================
  private RoleUser resolveRegistrationRole(String roleValue) {
    if (roleValue == null || roleValue.isBlank()) {
      return RoleUser.CLIENT;
    }

    String normalized = roleValue.trim().toUpperCase(Locale.ROOT);

    return switch (normalized) {
      case "ADMIN"  -> RoleUser.ADMIN;
      case "OWNER"  -> RoleUser.OWNER;  // ← manquait
      default       -> RoleUser.CLIENT;
    };
  }
}
