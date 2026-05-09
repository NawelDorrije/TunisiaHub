// AuthResponse.java
package org.example.backend_tunisiahub.Security;

import lombok.AllArgsConstructor;
import lombok.Getter;
<<<<<<< HEAD
=======

import lombok.NoArgsConstructor;


>>>>>>> origin/feature/integrated-app-event
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
<<<<<<< HEAD
public class AuthResponse {
=======

@NoArgsConstructor
public class AuthResponse {



    private Long id;

>>>>>>> origin/feature/integrated-app-event
    private String token;
    private String role;
    private String email;
    private String nom;
    private String prenom;
<<<<<<< HEAD
    private Long id;
}
=======

    //private Long id;
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
>>>>>>> origin/feature/integrated-app-event
