package org.example.backend_tunisiahub.Services.SouvenirsShops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiImageDescriptionService {

    private static final String GEMINI_API_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final int SHOP_DESCRIPTION_MAX_WORDS = 24;
    private static final int PRODUCT_DESCRIPTION_MAX_WORDS = 28;

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api.key.image:${GEMINI_API_KEY_2:${GEMINI_API_KEY:${GOOGLE_API_KEY:}}}}")
    private String geminiApiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-2.5-flash}}")
    private String geminiModel;

    private volatile boolean missingGeminiKeyLogged;

    public String generateShopDescription(MultipartFile file, String shopName, String category, String city) {
        String fallback = buildShopFallback(shopName, category, city);
        return generateFromMultipart(
                file,
                buildShopPrompt(shopName, category, city),
                fallback,
                220,
                SHOP_DESCRIPTION_MAX_WORDS
        );
    }

    public String generateProductDescription(
            MultipartFile file,
            String productName,
            String shopName,
            BigDecimal price
    ) {
        String fallback = buildProductFallback(productName, shopName, price);
        return generateFromMultipart(
                file,
                buildProductPrompt(productName, shopName, price),
                fallback,
                240,
                PRODUCT_DESCRIPTION_MAX_WORDS
        );
    }

    public String generateShopDescriptionFromUrl(Shop shop) {
        String fallback = buildShopFallback(shop.getName(), stringify(shop.getCategory()), shop.getCity());
        return generateFromImageUrl(
                shop.getPhotoUrl(),
                buildShopPrompt(shop.getName(), stringify(shop.getCategory()), shop.getCity()),
                fallback,
                220,
                SHOP_DESCRIPTION_MAX_WORDS
        );
    }

    public String generateProductDescriptionFromUrl(Product product) {
        String shopName = product.getShop() != null ? product.getShop().getName() : null;
        String fallback = buildProductFallback(product.getName(), shopName, product.getPrice());
        return generateFromImageUrl(
                product.getPhotoUrl(),
                buildProductPrompt(product.getName(), shopName, product.getPrice()),
                fallback,
                240,
                PRODUCT_DESCRIPTION_MAX_WORDS
        );
    }

    private String generateFromMultipart(
            MultipartFile file,
            String prompt,
            String fallback,
            int maxLength,
            int maxWords
    ) {
        if (file == null || file.isEmpty()) {
            return shorten(fallback, maxLength, maxWords);
        }
        try {
            String mimeType = file.getContentType();
            return generateDescription(file.getBytes(), mimeType, prompt, fallback, maxLength, maxWords);
        } catch (IOException ex) {
            log.warn("Failed to read uploaded image for AI description. Falling back. reason={}", ex.getMessage());
            return shorten(fallback, maxLength, maxWords);
        }
    }

    private String generateFromImageUrl(
            String imageUrl,
            String prompt,
            String fallback,
            int maxLength,
            int maxWords
    ) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return shorten(fallback, maxLength, maxWords);
        }
        if (!isGeminiConfigured()) {
            return shorten(fallback, maxLength, maxWords);
        }

        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .toEntity(byte[].class);
            String mimeType = response.getHeaders().getContentType() != null
                    ? response.getHeaders().getContentType().toString()
                    : MediaType.IMAGE_JPEG_VALUE;
            byte[] imageBytes = response.getBody();
            if (imageBytes == null || imageBytes.length == 0) {
                return shorten(fallback, maxLength, maxWords);
            }
            return generateDescription(imageBytes, mimeType, prompt, fallback, maxLength, maxWords);
        } catch (Exception ex) {
            log.warn("Failed to fetch image URL for AI description. Falling back. reason={}", ex.getMessage());
            return shorten(fallback, maxLength, maxWords);
        }
    }

    private String generateDescription(
            byte[] imageBytes,
            String mimeType,
            String prompt,
            String fallback,
            int maxLength,
            int maxWords
    ) {
        if (!isGeminiConfigured()) {
            return shorten(fallback, maxLength, maxWords);
        }

        try {
            String url = GEMINI_API_BASE.formatted(geminiModel, geminiApiKey);
            String requestBody = objectMapper.writeValueAsString(buildVisionRequest(prompt, imageBytes, sanitizeMimeType(mimeType)));

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String text = extractGeminiText(response);
            if (text == null || text.isBlank()) {
                String apiError = extractGeminiError(response);
                if (apiError != null) {
                    log.warn("Gemini returned an error while generating image description. model={} error={}", geminiModel, apiError);
                } else {
                    log.warn("Gemini returned an empty image description. model={}", geminiModel);
                }
                return shorten(fallback, maxLength, maxWords);
            }
            return shorten(text.trim(), maxLength, maxWords);
        } catch (RestClientResponseException ex) {
            log.warn(
                    "Gemini request failed for image description. model={} status={} response={}",
                    geminiModel,
                    ex.getStatusCode(),
                    truncateForLog(ex.getResponseBodyAsString())
            );
            return shorten(fallback, maxLength, maxWords);
        } catch (Exception ex) {
            log.warn("Failed to generate Gemini image description. Falling back. reason={}", ex.getMessage());
            return shorten(fallback, maxLength, maxWords);
        }
    }

    private Object buildVisionRequest(String prompt, byte[] imageBytes, String mimeType) {
        return java.util.Map.of(
                "contents", java.util.List.of(
                        java.util.Map.of(
                                "parts", java.util.List.of(
                                        java.util.Map.of("text", prompt),
                                        java.util.Map.of(
                                                "inlineData",
                                                java.util.Map.of(
                                                        "mimeType", mimeType,
                                                        "data", Base64.getEncoder().encodeToString(imageBytes)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private String extractGeminiText(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }
        for (JsonNode part : parts) {
            String text = part.path("text").asText(null);
            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private String extractGeminiError(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        String errorMessage = root.path("error").path("message").asText(null);
        if (errorMessage == null || errorMessage.isBlank()) {
            return null;
        }
        return errorMessage.trim();
    }

    private String buildShopPrompt(String shopName, String category, String city) {
        return """
                You are writing a polished, personalized shop description for a Tunisia travel and souvenir app.

                Shop name: %s
                Category: %s
                City: %s

                Analyze this specific uploaded shop image and write one short, attractive paragraph for this exact shop.
                Base the description on visible details from the image first.
                Identify the most likely shop category or specialty from what is visible.
                Mention visible atmosphere, decor, layout, product presentation, and the kind of items the shop appears to focus on.
                Make it feel personal and descriptive, not generic.
                Keep it natural, specific, and marketing-friendly.
                Do not invent facts that are not visible or provided.
                Do not mention AI, image analysis, or uncertainty.
                Do not use phrases like "seems to", "appears to", or "probably".
                Keep it very short: maximum 2 lines, around 20 to 24 words, and only 1 sentence.
                """.formatted(defaultValue(shopName, "Unnamed shop"), defaultValue(category, "Not specified"), defaultValue(city, "Tunisia"));
    }

    private String buildProductPrompt(String productName, String shopName, BigDecimal price) {
        return """
                You are writing a polished, personalized product description for a Tunisia souvenir marketplace.

                Product name: %s
                Shop name: %s
                Price: %s

                Analyze this specific uploaded product image and write one short, attractive paragraph for this exact item.
                Base the description on visible details from the image first.
                Describe visible materials, colors, texture, finish, shape, patterns, and craftsmanship.
                Mention what makes this item visually distinctive and appealing.
                If relevant, mention whether it works well as decor, a keepsake, or a gift.
                Keep it natural, specific, and sales-friendly.
                Do not invent hidden details that are not visible or provided.
                Do not mention AI, image analysis, or uncertainty.
                Do not use phrases like "seems to", "appears to", or "probably".
                Keep it very short: maximum 2 lines, around 24 to 28 words, and only 1 sentence.
                """.formatted(
                defaultValue(productName, "Unnamed product"),
                defaultValue(shopName, "TunisiaHub shop"),
                price != null ? price.toPlainString() : "Not specified"
        );
    }

    private String buildShopFallback(String shopName, String category, String city) {
        return "%s is a welcoming %s shop%s, offering visitors a curated local experience with products and a style inspired by Tunisian craftsmanship."
                .formatted(
                        defaultValue(shopName, "This shop"),
                        defaultValue(category, "souvenir"),
                        city != null && !city.isBlank() ? " in " + city : ""
                );
    }

    private String buildProductFallback(String productName, String shopName, BigDecimal price) {
        String pricePart = price != null ? " at " + price.toPlainString() + " TND" : "";
        return "%s is a distinctive item from %s, selected for its appealing look, gift potential, and connection to Tunisian artisan style%s."
                .formatted(
                        defaultValue(productName, "This product"),
                        defaultValue(shopName, "our shop"),
                        pricePart
                );
    }

    private String sanitizeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        return mimeType;
    }

    private String stringify(Object value) {
        return value != null ? value.toString() : null;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isGeminiConfigured() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return true;
        }
        if (!missingGeminiKeyLogged) {
            synchronized (this) {
                if (!missingGeminiKeyLogged) {
                    log.warn("Gemini API key is missing. Configure gemini.api.key.image or GEMINI_API_KEY_2.");
                    missingGeminiKeyLogged = true;
                }
            }
        }
        return false;
    }

    private String truncateForLog(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300) + "...";
    }

    private String shorten(String value, int maxLength, int maxWords) {
        return truncateByLength(truncateByWords(value, maxWords), maxLength);
    }

    private String truncateByWords(String value, int maxWords) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return normalized;
        }
        String[] words = normalized.split(" ");
        if (words.length <= maxWords) {
            return normalized;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        return builder.append("...").toString();
    }

    private String truncateByLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength - 2).trim() + "...";
    }
}
