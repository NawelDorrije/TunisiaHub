// LoginRequest.java
package org.example.backend_tunisiahub.Security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}