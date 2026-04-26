// AuthResponse.java
package org.example.backend_tunisiahub.Security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private Long id ;
    private String token;
    private String role;
    private String email;
    private String nom;
    private String prenom;

}