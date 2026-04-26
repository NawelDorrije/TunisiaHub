package org.example.backend_tunisiahub.Controllers.Restaurant;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchRequest;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchResponse;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Services.Restaurant.AiSearchService;
import org.example.backend_tunisiahub.Services.Restaurant.RestaurantService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AiSearchController {

    private static final int MAX_CANDIDATES = 25;

    private final RestaurantService restaurantService;
    private final AiSearchService aiSearchService;

    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> search(@Valid @RequestBody AiSearchRequest request) {
        try {
            List<Restaurant> candidates = restaurantService.searchAiCandidates(request, MAX_CANDIDATES);
            log.info("AI search pre-filter produced {} candidates for query='{}'", candidates.size(), request.query());

            if (candidates.isEmpty()) {
                return ResponseEntity.ok(new AiSearchResponse("No matching restaurants found.", List.of(), 0));
            }

            AiSearchResponse response = aiSearchService.search(request, candidates);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during AI restaurant search", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process AI restaurant search");
        }
    }
}
