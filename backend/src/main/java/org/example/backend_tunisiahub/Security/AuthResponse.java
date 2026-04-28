// AuthResponse.java
package org.example.backend_tunisiahub.Security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String email;
    private String nom;
    private String prenom;
    private Long id;
    private AuthUser user;

    public AuthResponse(String token, String role, String email, String nom, String prenom, Long id) {
        this.token = token;
        this.role = role;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.id = id;
        this.user = new AuthUser(id, email, role);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthUser {
        private Long id;
        private String email;
        private String role;
    }
}
