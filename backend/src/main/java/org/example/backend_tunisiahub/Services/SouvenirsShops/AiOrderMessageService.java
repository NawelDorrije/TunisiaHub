package org.example.backend_tunisiahub.Services.SouvenirsShops;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiOrderMessageService {

    private static final String GEMINI_API_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api.key:${GEMINI_API_KEY:${GOOGLE_API_KEY:}}}")
    private String geminiApiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-2.5-flash}}")
    private String geminiModel;

    public String generateSmartStatusMessage(Order order, List<OrderItem> orderItems) {
        String prompt = buildPrompt(order, orderItems);
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return buildFallbackMessage(order, orderItems);
        }

        try {
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

            String text = extractGeminiText(response);
            if (text == null || text.isBlank()) {
                return buildFallbackMessage(order, orderItems);
            }
            return text.trim();
        } catch (Exception ex) {
            log.warn("Failed to generate Gemini order status message. Falling back. reason={}", ex.getMessage());
            return buildFallbackMessage(order, orderItems);
        }
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
        return parts.get(0).path("text").asText(null);
    }

    private String buildPrompt(Order order, List<OrderItem> orderItems) {
        String shopName = order.getShop() != null ? order.getShop().getName() : "our shop";
        String firstName = order.getUser() != null && order.getUser().getPrenom() != null
                ? order.getUser().getPrenom()
                : "Client";
        String products = orderItems == null || orderItems.isEmpty()
                ? "N/A"
                : orderItems.stream()
                .map(item -> item.getProduct().getName() + " ×" + item.getQuantity())
                .collect(Collectors.joining(", "));

        String expectedDelivery = mapExpectedDelivery(order.getStatus());

        return """
                You are a friendly shop assistant for "%s".

                Client first name: %s
                Order contains: %s
                New status: %s
                Expected delivery: %s

                Write a short, warm, personalized message (maximum 2 sentences) for the client.
                Make it sound human, positive, and specific to the product and shop.
                Never mention AI or that the message was generated.
                """.formatted(shopName, firstName, products, order.getStatus().name(), expectedDelivery);
    }

    private String mapExpectedDelivery(OrderStatus status) {
        if (status == null) {
            return "N/A";
        }
        return switch (status) {
            case PROCESSING -> "2-3 days";
            case DELIVERED, COMPLETED -> "Today";
            case PAID -> "3-5 days";
            case PENDING -> "To be confirmed soon";
            case CANCELLED -> "N/A";
        };
    }

    private String buildFallbackMessage(Order order, List<OrderItem> orderItems) {
        String firstName = order.getUser() != null && order.getUser().getPrenom() != null
                ? order.getUser().getPrenom()
                : "Client";
        String shopName = order.getShop() != null ? order.getShop().getName() : "our shop";
        String firstProduct = (orderItems != null && !orderItems.isEmpty() && orderItems.get(0).getProduct() != null)
                ? orderItems.get(0).getProduct().getName()
                : "your order";
        return "Great news " + firstName + "! " + firstProduct + " from " + shopName
                + " is now " + order.getStatus().name() + ".";
    }
}
