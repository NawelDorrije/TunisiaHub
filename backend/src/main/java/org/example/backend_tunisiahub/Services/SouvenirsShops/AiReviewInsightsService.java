package org.example.backend_tunisiahub.Services.SouvenirsShops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.OwnerReviewInsightsResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReviewInsightsService {

    private static final String GEMINI_API_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "have", "from", "your", "very", "just",
            "good", "nice", "great", "really", "were", "been", "they", "them", "their", "about",
            "would", "there", "after", "before", "more", "less", "tout", "tres", "avec", "pour",
            "dans", "sans", "plus", "moins", "mais", "sont", "etre", "etait", "service", "product",
            "shop", "owner", "store", "item", "items", "produit", "boutique"
    );

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api.key:${GEMINI_API_KEY:${GOOGLE_API_KEY:}}}")
    private String geminiApiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-2.5-flash}}")
    private String geminiModel;

    public OwnerReviewInsightsResponse generateOwnerInsights(
            User owner,
            List<Shop> shops,
            List<Product> products,
            List<Review> shopReviews,
            List<Review> productReviews
    ) {
        OwnerReviewInsightsResponse fallback = buildFallbackInsights(owner, shopReviews, productReviews);
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return fallback;
        }

        try {
            String prompt = buildPrompt(owner, shops, products, shopReviews, productReviews);
            String url = GEMINI_API_BASE.formatted(geminiModel, geminiApiKey);
            String requestBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": %s
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(objectMapper.writeValueAsString(prompt));

            String response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            OwnerReviewInsightsResponse parsed = extractInsights(response, fallback);
            if (parsed == null) {
                return fallback;
            }

            parsed.setOwnerId(owner.getId());
            parsed.setOwnerName(buildOwnerName(owner));
            parsed.setTotalReviews(shopReviews.size() + productReviews.size());
            parsed.setShopReviewCount(shopReviews.size());
            parsed.setProductReviewCount(productReviews.size());
            parsed.setAverageShopRating(averageRating(shopReviews));
            parsed.setAverageProductRating(averageRating(productReviews));
            parsed.setGeneratedWithAi(true);
            return parsed;
        } catch (Exception ex) {
            log.warn("Failed to generate Gemini review insights. Falling back. reason={}", ex.getMessage());
            return fallback;
        }
    }

    private OwnerReviewInsightsResponse extractInsights(String rawJson, OwnerReviewInsightsResponse fallback) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }

        String text = null;
        for (JsonNode part : parts) {
            String candidate = part.path("text").asText(null);
            if (candidate != null && !candidate.isBlank()) {
                text = candidate.trim();
                break;
            }
        }
        if (text == null || text.isBlank()) {
            return null;
        }

        JsonNode parsed = objectMapper.readTree(stripCodeFence(text));
        List<String> bestFeatures = new ArrayList<>();
        JsonNode featuresNode = parsed.path("bestFeatures");
        if (featuresNode.isArray()) {
            for (JsonNode feature : featuresNode) {
                String value = feature.asText(null);
                if (value != null && !value.isBlank()) {
                    bestFeatures.add(value.trim());
                }
            }
        }
        if (bestFeatures.isEmpty()) {
            bestFeatures = fallback.getBestFeatures();
        }

        return new OwnerReviewInsightsResponse(
                fallback.getOwnerId(),
                fallback.getOwnerName(),
                fallback.getTotalReviews(),
                fallback.getShopReviewCount(),
                fallback.getProductReviewCount(),
                fallback.getAverageShopRating(),
                fallback.getAverageProductRating(),
                parsed.path("summary").asText(fallback.getSummary()),
                parsed.path("mainProblem").asText(fallback.getMainProblem()),
                parsed.path("mostFrequentProblem").asText(fallback.getMostFrequentProblem()),
                bestFeatures,
                fallback.getShopReviews(),
                fallback.getProductReviews(),
                true
        );
    }

    private String buildPrompt(
            User owner,
            List<Shop> shops,
            List<Product> products,
            List<Review> shopReviews,
            List<Review> productReviews
    ) {
        return """
                You analyze customer reviews for one souvenir business owner.

                Return strict JSON only with this exact shape:
                {
                  "summary": "short paragraph",
                  "mainProblem": "single sentence",
                  "mostFrequentProblem": "single sentence",
                  "bestFeatures": ["feature 1", "feature 2", "feature 3"]
                }

                Rules:
                - Base the answer only on the provided reviews and ratings.
                - Do not invent issues or strengths.
                - If there is no clear problem, say that clearly.
                - bestFeatures must contain 1 to 3 concise strings.
                - summary should mention the dominant positives and negatives.

                Owner: %s
                Shop count: %d
                Product count: %d
                Shop review average: %.2f
                Product review average: %.2f

                Shops:
                %s

                Products:
                %s

                Shop reviews:
                %s

                Product reviews:
                %s
                """.formatted(
                buildOwnerName(owner),
                shops.size(),
                products.size(),
                averageRating(shopReviews),
                averageRating(productReviews),
                formatShops(shops),
                formatProducts(products),
                formatReviews(shopReviews, "SHOP"),
                formatReviews(productReviews, "PRODUCT")
        );
    }

    private String formatShops(List<Shop> shops) {
        if (shops.isEmpty()) {
            return "- none";
        }
        StringBuilder builder = new StringBuilder();
        for (Shop shop : shops) {
            builder.append("- ").append(shop.getId()).append(": ").append(shop.getName()).append('\n');
        }
        return builder.toString().trim();
    }

    private String formatProducts(List<Product> products) {
        if (products.isEmpty()) {
            return "- none";
        }
        StringBuilder builder = new StringBuilder();
        for (Product product : products) {
            builder.append("- ").append(product.getId()).append(": ").append(product.getName());
            if (product.getShop() != null) {
                builder.append(" (shop: ").append(product.getShop().getName()).append(')');
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private String formatReviews(List<Review> reviews, String kind) {
        if (reviews.isEmpty()) {
            return "- none";
        }
        StringBuilder builder = new StringBuilder();
        for (Review review : reviews) {
            String targetName = kind;
            if ("SHOP".equals(kind) && review.getShop() != null) {
                targetName = review.getShop().getName();
            } else if ("PRODUCT".equals(kind) && review.getProduct() != null) {
                targetName = review.getProduct().getName();
            }
            builder.append("- [")
                    .append(kind)
                    .append("] ")
                    .append(targetName)
                    .append(" | rating=")
                    .append(review.getRating())
                    .append(" | comment=")
                    .append(normalizeComment(review.getComment()))
                    .append('\n');
        }
        return builder.toString().trim();
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return "(no comment)";
        }
        return comment.replaceAll("\\s+", " ").trim();
    }

    private String stripCodeFence(String text) {
        String normalized = text.trim();
        if (normalized.startsWith("```")) {
            int lineBreak = normalized.indexOf('\n');
            if (lineBreak >= 0) {
                normalized = normalized.substring(lineBreak + 1);
            }
            if (normalized.endsWith("```")) {
                normalized = normalized.substring(0, normalized.length() - 3);
            }
        }
        return normalized.trim();
    }

    private OwnerReviewInsightsResponse buildFallbackInsights(
            User owner,
            List<Review> shopReviews,
            List<Review> productReviews
    ) {
        List<Review> allReviews = new ArrayList<>();
        allReviews.addAll(shopReviews);
        allReviews.addAll(productReviews);

        List<Review> lowRated = allReviews.stream()
                .filter(review -> review.getRating() != null && review.getRating() <= 3)
                .toList();
        List<Review> highRated = allReviews.stream()
                .filter(review -> review.getRating() != null && review.getRating() >= 4)
                .toList();

        List<String> issueKeywords = extractTopKeywords(lowRated);
        List<String> strengthKeywords = extractTopKeywords(highRated);
        if (strengthKeywords.isEmpty()) {
            strengthKeywords = List.of("No clear standout strength detected yet");
        }

        String mainProblem = lowRated.isEmpty()
                ? "No major recurring problem stands out in the current reviews."
                : "The main issue is " + describeKeywords(issueKeywords, "inconsistent customer experience") + ".";
        String mostFrequentProblem = lowRated.isEmpty()
                ? "No frequent complaint was detected from the current review set."
                : "The most frequent complaint is " + describeKeywords(issueKeywords, "mixed service quality") + ".";

        String summary;
        if (allReviews.isEmpty()) {
            summary = "There are no active reviews yet for this owner's shops and products.";
        } else {
            summary = "Across " + allReviews.size() + " active reviews, the overall sentiment is "
                    + String.format(Locale.US, "%.2f/5", averageRating(allReviews))
                    + ". Shop reviews average "
                    + String.format(Locale.US, "%.2f/5", averageRating(shopReviews))
                    + " and product reviews average "
                    + String.format(Locale.US, "%.2f/5", averageRating(productReviews))
                    + ". Customers most often praise " + String.join(", ", strengthKeywords) + ".";
        }

        return new OwnerReviewInsightsResponse(
                owner.getId(),
                buildOwnerName(owner),
                allReviews.size(),
                shopReviews.size(),
                productReviews.size(),
                averageRating(shopReviews),
                averageRating(productReviews),
                summary,
                mainProblem,
                mostFrequentProblem,
                strengthKeywords,
                shopReviews,
                productReviews,
                false
        );
    }

    private List<String> extractTopKeywords(List<Review> reviews) {
        Map<String, Integer> frequency = new HashMap<>();
        for (Review review : reviews) {
            if (review.getComment() == null || review.getComment().isBlank()) {
                continue;
            }
            Set<String> seen = new HashSet<>();
            String[] tokens = review.getComment()
                    .toLowerCase(Locale.ROOT)
                    .split("[^\\p{L}\\p{Nd}]+");
            for (String token : tokens) {
                if (token.length() < 4 || STOP_WORDS.contains(token) || !seen.add(token)) {
                    continue;
                }
                frequency.merge(token, 1, Integer::sum);
            }
        }

        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(entry -> humanizeKeyword(entry.getKey()))
                .toList();
    }

    private String humanizeKeyword(String keyword) {
        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("delivery", "delivery speed");
        replacements.put("shipping", "shipping speed");
        replacements.put("quality", "product quality");
        replacements.put("service", "customer service");
        replacements.put("packaging", "packaging");
        replacements.put("price", "pricing");
        replacements.put("prices", "pricing");
        replacements.put("beautiful", "beautiful design");
        replacements.put("friendly", "friendly service");
        replacements.put("craftsmanship", "craftsmanship");
        replacements.put("artisan", "artisan craftsmanship");
        return replacements.getOrDefault(keyword, keyword);
    }

    private String describeKeywords(List<String> keywords, String fallback) {
        if (keywords == null || keywords.isEmpty()) {
            return fallback;
        }
        if (keywords.size() == 1) {
            return keywords.get(0);
        }
        if (keywords.size() == 2) {
            return keywords.get(0) + " and " + keywords.get(1);
        }
        return keywords.get(0) + ", " + keywords.get(1) + ", and " + keywords.get(2);
    }

    private String buildOwnerName(User owner) {
        String firstName = owner.getPrenom() == null ? "" : owner.getPrenom().trim();
        String lastName = owner.getNom() == null ? "" : owner.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? owner.getEmail() : fullName;
    }

    private double averageRating(List<Review> reviews) {
        return reviews.stream()
                .filter(review -> review.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
