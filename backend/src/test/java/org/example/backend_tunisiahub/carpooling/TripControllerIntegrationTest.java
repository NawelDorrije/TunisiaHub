package org.example.backend_tunisiahub.carpooling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TripControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        private Map<String, Object> validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new HashMap<>();
        validRequest.put("departurePoint", "Tunis");
        validRequest.put("destination", "Sousse");
        validRequest.put("departureDateTime", LocalDateTime.now().plusDays(2));
        validRequest.put("price", 25);
        validRequest.put("seatsTotal", 3);
        validRequest.put("vehicleId", 1L);
    }

    @Test
    void createTripDriverSuccess() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "10")
                        .header("X-ROLE", "USER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value("10"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.seatsTotal").value(3))
                .andExpect(jsonPath("$.seatsAvailable").value(3));
    }

    @Test
    void createTripFailsIfNotDriver() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "11")
                        .header("X-ROLE", "INVALID_ROLE")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listTripsReturnsCreatedTrip() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "42")
                        .header("X-ROLE", "USER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/carpooling/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].departurePoint").value("Tunis"))
                .andExpect(jsonPath("$[0].destination").value("Sousse"));
    }

    @Test
    void cancelTripOnlyByOwner() throws Exception {
        String response = mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "100")
                        .header("X-ROLE", "USER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tripId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/carpooling/trips/{id}/cancel", tripId)
                        .header("X-USER-ID", "999")
                        .header("X-ROLE", "USER"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/carpooling/trips/{id}/cancel", tripId)
                        .header("X-USER-ID", "100")
                        .header("X-ROLE", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}
