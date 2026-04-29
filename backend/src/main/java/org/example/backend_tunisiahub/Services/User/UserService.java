package org.example.backend_tunisiahub.Services.User;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    public List<User> retrieveAllUsers() {
        assertAdmin();
        return userRepository.findAll();
    }

    @Override
    public User retrieveUser(Long id) {
        assertAdmin();
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User addUser(User user) {

        // ===== from feature/camping (kept logic) =====
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getRole() == null) {
            user.setRole(RoleUser.CLIENT);
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        assertAdmin();
        userRepository.deleteById(id);
    }

    @Override
    public User modifyUser(User user) {
        assertAdmin();
        return userRepository.save(user);
    }

    // ================= ADMIN CHECK =================
    private void assertAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
            authentication.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {

            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can access users");
        }
    }

    // ================= CURRENT USER =================
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }

        return authentication.getName();
    }
}