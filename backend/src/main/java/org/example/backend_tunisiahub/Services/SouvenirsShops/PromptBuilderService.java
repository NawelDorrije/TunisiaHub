package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.math.BigDecimal;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.GeneratePromotionRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    // ── Caption ───────────────────────────────────────────────────────────────

    public String buildCaptionPrompt(
            Shop shop,
            Product product,
            Product topProduct,
            GeneratePromotionRequest choices
    ) {
        String language        = defaultValue(choices.getLanguage(), "French");
        String platform        = defaultValue(choices.getPlatform(), "Instagram");
        String tone            = defaultValue(choices.getTone(), "warm and persuasive");
        String shopName        = shop != null ? defaultValue(shop.getName(), "Unnamed shop") : "TunisiaHub shop";
        String category        = shop != null ? defaultValue(stringify(shop.getCategory()), "souvenir") : "souvenir";
        String city            = shop != null ? defaultValue(shop.getCity(), "Tunisia") : "Tunisia";
        String shopDescription = shop != null
                ? defaultValue(shop.getDescription(), "Local Tunisian products and artisanal creations.")
                : "Local Tunisian products and artisanal creations.";
        String topProductName  = topProduct != null ? defaultValue(topProduct.getName(), "Featured product") : "Featured product";
        String topProductPrice = formatPrice(topProduct != null ? topProduct.getPrice() : null);

        String focusLine = product != null
                ? "Focus strongly on this featured item: "
                + defaultValue(product.getName(), "Unnamed product")
                + " priced at " + formatPrice(product.getPrice()) + "."
                : "Promote the shop as a full destination while highlighting the featured product naturally.";

        return """
                You are a professional social media marketing expert specialized in the Tunisian market.
                Write a compelling %s caption in %s for %s.

                Business details:
                - Shop name: %s
                - Category: %s
                - City: %s
                - Description: %s
                - Top product: %s at %s

                %s
                Tone: %s.

                Requirements:
                - Keep it authentic, persuasive, and specific to this business.
                - Mention at least one concrete detail from the business description.
                - Include 5 relevant hashtags.
                - Include exactly 3 emojis.
                - End with a strong call to action.
                - Maximum 120 words.
                - Never mention AI or that this caption was generated.
                """.formatted(
                platform,
                language,
                platform,
                shopName,
                category,
                city,
                shopDescription,
                topProductName,
                topProductPrice,
                focusLine,
                tone
        );
    }

    // ── Image ─────────────────────────────────────────────────────────────────

    public String buildImagePrompt(
            Shop shop,
            Product product,
            GeneratePromotionRequest choices
    ) {
        String colorTheme = defaultValue(choices.getColorTheme(), "earthy warm tones");
        String mood       = defaultValue(choices.getTone(), "premium and inviting");
        String category   = shop != null ? defaultValue(stringify(shop.getCategory()), "artisan") : "artisan";
        String city       = shop != null ? defaultValue(shop.getCity(), "Tunis") : "Tunis";
        // Fix 1: Explicit product context in subject
        String subject = product != null
                ? defaultValue(product.getName(), "artisan product")
                : category + " products, " + defaultValue(shop != null ? shop.getName() : null, "boutique") + " collection";

        String moodStyle = switch (mood.toLowerCase()) {
            case "luxury", "luxurious", "premium and inviting" ->
                    "ultra luxury fashion editorial photography, Vogue magazine cover aesthetic, " +
                            "aspirational lifestyle imagery, dramatic chiaroscuro shadows, " +
                            "gold leaf accents, silk and velvet textures, high fashion glamour";

            case "bold", "bold and energetic" ->
                    "bold graphic advertising poster, high contrast composition, " +
                            "striking dynamic diagonal lines, powerful visual energy, " +
                            "modern urban street style, punchy and impactful";

            case "minimal", "calm and elegant" ->
                    "minimalist luxury aesthetic, vast negative space, " +
                            "precise geometric composition, soft diffused studio light, " +
                            "understated Scandinavian elegance, gallery-quality still life";

            case "friendly", "warm and persuasive" ->
                    "warm inviting lifestyle photography, cozy golden hour atmosphere, " +
                            "authentic Tunisian market feeling, natural soft daylight, " +
                            "approachable and genuine human warmth without people";

            case "urgent", "urgent and exciting" ->
                    "high impact promotional visual, bold sale campaign aesthetic, " +
                            "vivid saturated colors, strong dynamic focal point, " +
                            "eye-catching asymmetric composition, electric energy";

            default ->
                    "professional commercial photography, clean editorial composition, " +
                            "polished advertising quality";
        };

        String colorStyle = switch (colorTheme.toLowerCase()) {
            case "warm", "earthy warm tones" ->
                    "warm amber and terracotta color palette, golden hour glow, " +
                            "rich deep ochre and burnt sienna tones, honey and saffron accents, " +
                            "sun-drenched Mediterranean warmth";

            case "dark", "dark and moody" ->
                    "deep dramatic dark background, midnight navy and charcoal tones, " +
                            "low-key cinematic lighting, mysterious sophisticated noir atmosphere, " +
                            "subtle jewel-tone highlights";

            case "light", "soft and minimal" ->
                    "bright airy high-key lighting, pure white and cream background, " +
                            "soft pastel accents, clean Scandinavian light, " +
                            "fresh and pristine elegance";

            case "vibrant", "bright and vibrant" ->
                    "vivid jewel-tone color palette, electric cobalt blue and magenta, " +
                            "bold color blocking, saturated energetic hues, " +
                            "eye-popping chromatic contrast";

            default -> "balanced natural color grading, professional color palette";
        };

        // Fix 3: Category-specific visual direction
        String categoryVisual = switch (category.toLowerCase()) {
            case "clothing", "leather", "fashion" ->
                    "luxury leather handbags and accessories arranged on a dark marble surface, " +
                    "close-up product hero shot, studio lighting";
            case "jewelry" ->
                    "fine jewelry pieces on black velvet, macro photography, " +
                    "sparkling gems, studio lighting";
            case "food", "restaurant" ->
                    "gourmet food plating, overhead shot, fine dining presentation";
            case "art", "crafts", "souvenir" ->
                    "handcrafted artisan objects arranged as flat lay, " +
                    "natural linen background, product photography";
            default ->
                    "product hero shot on neutral background, studio lighting, " +
                    "commercial photography";
        };

        return ("NOT a travel photo, NOT a landscape, NOT a street scene, commercial studio advertisement. "
                + "A stunning high-end advertisement poster for a " + category + " brand called \""
                + subject + "\" from " + city + ", Tunisia. "
                + moodStyle + ". "
                + colorStyle + ". "
                + "Shot with a Phase One medium format camera, 8K ultra resolution, "
                + "razor-sharp focus, cinematic depth of field with bokeh background, "
                + "award-winning commercial advertising photography, "
                + "perfectly balanced composition with generous empty space for text overlay, "
                + "artisanal Tunisian craftsmanship heritage, "
                + "luxurious product styling and prop arrangement, "
                + "professional retouching and color grading, "
                + "magazine double-page spread quality. "
                + categoryVisual + ". "
                // Fix 2: Strong negative prompts
                + "AVOID: street markets, people, crowds, food, vegetables, outdoor scenes, "
                + "tourism photography, travel photography, medina scenes. "
                + "SHOW: the actual product isolated, studio setting, product hero shot. "
                + "No text, no words, no letters, no numbers, no watermark, no logo, "
                + "no people, no faces, no hands.")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String stringify(Object value) {
        return value == null ? null : value.toString().toLowerCase().replace('_', ' ');
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? "N/A" : price.stripTrailingZeros().toPlainString() + " TND";
    }
}