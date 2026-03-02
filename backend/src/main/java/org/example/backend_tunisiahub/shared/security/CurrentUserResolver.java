package org.example.backend_tunisiahub.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    public static final String USER_ID_HEADER = "X-USER-ID";
    public static final String ROLE_HEADER = "X-ROLE";

    public Long getUserId(HttpServletRequest request) {
        String value = request.getHeader(USER_ID_HEADER);
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing required header: X-USER-ID");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid X-USER-ID header value");
        }
    }

    public String getRole(HttpServletRequest request) {
        String value = request.getHeader(ROLE_HEADER);
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing required header: X-ROLE");
        }
        return value.trim().toUpperCase();
    }
}
