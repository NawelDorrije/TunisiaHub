package org.example.backend_tunisiahub.Controllers;

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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null)
            return ResponseEntity.badRequest().body("Email already exists");

        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleUser.CLIENT);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name(), user.getEmail(), user.getNom(), user.getPrenom(), user.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody RegisterRequest request) {
        return register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null)
            return ResponseEntity.status(404).body("User not found");

        String storedPassword = user.getMotDePasse();
        boolean validPassword = false;
        if (storedPassword != null) {
            try {
                validPassword = passwordEncoder.matches(request.getPassword(), storedPassword);
            } catch (IllegalArgumentException ignored) {
                validPassword = false;
            }
        }

        // Backward compatibility: allow legacy plain-text passwords, then migrate to BCrypt.
        if (!validPassword && storedPassword != null && storedPassword.equals(request.getPassword())) {
            user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            validPassword = true;
        }

        if (!validPassword)
            return ResponseEntity.status(401).body("Invalid password");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name(), user.getEmail(), user.getNom(), user.getPrenom(), user.getId()));
    }
}
