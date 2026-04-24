package org.example.backend_tunisiahub.Services.SouvenirsShops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.GeneratePromotionRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final ShopRepository       shopRepository;
    private final ProductRepository    productRepository;
    private final UserRepository       userRepository;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper         objectMapper;
    private final RestClient           restClient = RestClient.create();

    @Value("${groq.api-key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

    // ── Caption ───────────────────────────────────────────────────────────────

    public String generateCaption(GeneratePromotionRequest request) {
        log.info("generateCaption — shopId={} productId={}",
                request == null ? null : request.getShopId(),
                request == null ? null : request.getProductId());

        PromotionData data       = resolvePromotionData(request);
        Product       topProduct = findTopProduct(data.shop());
        String        prompt     = promptBuilderService.buildCaptionPrompt(
                data.shop(), data.product(), topProduct, request);

        return callGroq(prompt);
    }

    // ── Image — anonymous Pollinations (no API key needed) ────────────────────

    public String generateImageUrl(GeneratePromotionRequest request) {
        log.info("generateImageUrl — shopId={} productId={}",
                request == null ? null : request.getShopId(),
                request == null ? null : request.getProductId());

        PromotionData data   = resolvePromotionData(request);
        String        prompt = promptBuilderService.buildImagePrompt(
                data.shop(), data.product(), request);

        log.debug("Image prompt: {}", prompt);

        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8)
                .replace("+", "%20");

        String pollinationsUrl = "https://image.pollinations.ai/prompt/"
                + encodedPrompt
                + "?width=1080&height=1080&nologo=true&model=flux&seed="
                + System.currentTimeMillis() % 10000;

        log.info("Fetching image from Pollinations server-side: {}", pollinationsUrl);

        try {
            byte[] imageBytes = RestClient.create().get()
                    .uri(pollinationsUrl)
                    .retrieve()
                    .body(byte[].class);

            if (imageBytes == null || imageBytes.length == 0) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Pollinations returned empty image");
            }

            log.info("Image fetched successfully — {} bytes", imageBytes.length);
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:image/jpeg;base64," + base64;

        } catch (Exception ex) {
            log.warn("Pollinations fetch failed — reason={}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Image generation failed");
        }
    }

    // ── Groq ──────────────────────────────────────────────────────────────────

    private String callGroq(String prompt) {
        log.info("Calling Groq — model={}", groqModel);

        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("Groq API key is missing");
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Groq API key is missing");
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", groqModel,
                    "messages", List.of(Map.of(
                            "role",    "user",
                            "content", prompt
                    )),
                    "max_tokens", 500
            ));

            String rawResponse = restClient.post()
                    .uri(groqUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + groqApiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.debug("Groq raw response (truncated): {}", truncate(rawResponse));

            String text = extractGroqText(rawResponse);
            if (text == null || text.isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY,
                        "Groq response did not contain text content");
            }

            log.info("Caption generated — length={}", text.length());
            return text.trim();

        } catch (RestClientResponseException ex) {
            log.warn("Groq failed — status={} response={}",
                    ex.getStatusCode(), truncate(ex.getResponseBodyAsString()));
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Caption generation failed at AI provider");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Groq failed — reason={}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Caption generation failed");
        }
    }

    private String extractGroqText(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        return root.path("choices").path(0).path("message").path("content").asText(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PromotionData resolvePromotionData(GeneratePromotionRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        Long    shopId       = request.getShopId();
        Long    productId    = request.getProductId();
        boolean hasShopId    = shopId    != null;
        boolean hasProductId = productId != null;

        if (hasShopId == hasProductId) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Provide exactly one of shopId or productId");
        }

        if (hasShopId) {
            log.debug("Resolving shop id={}", shopId);
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
            assertOwnerOrAdmin(shop.getOwner() == null ? null : shop.getOwner().getEmail());
            return new PromotionData(shop, null);
        }

        log.debug("Resolving product id={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        Shop shop = product.getShop();
        if (shop == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not linked to a shop");
        }
        assertOwnerOrAdmin(shop.getOwner() == null ? null : shop.getOwner().getEmail());
        return new PromotionData(shop, product);
    }

    private Product findTopProduct(Shop shop) {
        List<Product> products = productRepository.findByShopId(shop.getId());
        return products.stream()
                .max(Comparator
                        .comparing(Product::getAverageRating,
                                Comparator.nullsFirst(Double::compareTo))
                        .thenComparing(Product::getPrice,
                                Comparator.nullsFirst(Comparable::compareTo)))
                .orElse(null);
    }

    private void assertOwnerOrAdmin(String ownerEmail) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == RoleUser.ADMIN) {
            log.debug("Access granted — ADMIN");
            return;
        }
        if (ownerEmail == null || !currentUser.getEmail().equalsIgnoreCase(ownerEmail)) {
            log.warn("Access denied — currentUser={} ownerEmail={}",
                    currentUser.getEmail(), ownerEmail);
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You are not allowed to generate promotions for this target");
        }
        log.debug("Access granted — owner");
    }

    private User getCurrentUser() {
        var auth  = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = (auth == null) ? null : auth.getName();
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }
        return user;
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 250 ? normalized : normalized.substring(0, 250) + "...";
    }

    private record PromotionData(Shop shop, Product product) {}
}