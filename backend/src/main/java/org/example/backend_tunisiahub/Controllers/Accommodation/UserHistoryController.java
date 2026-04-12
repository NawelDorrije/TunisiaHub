package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Services.Accommodation.UserHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    @PostMapping("/track/{accommodationId}")
    public ResponseEntity<?> trackView(
            @PathVariable Long accommodationId,
            @AuthenticationPrincipal String email) {
        userHistoryService.trackView(accommodationId, email);
        return ResponseEntity.ok().build();
    }
}