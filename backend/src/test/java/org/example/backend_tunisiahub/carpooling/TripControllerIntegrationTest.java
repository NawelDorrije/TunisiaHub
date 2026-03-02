package org.example.backend_tunisiahub.carpooling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend_tunisiahub.carpooling.dto.TripCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private TripCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new TripCreateRequest();
        validRequest.setDeparturePoint("Tunis");
        validRequest.setDestination("Sousse");
        validRequest.setDepartureDateTime(LocalDateTime.now().plusDays(2));
        validRequest.setPrice(BigDecimal.valueOf(25));
        validRequest.setSeatsTotal(3);
    }

    @Test
    void createTripDriverSuccess() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "10")
                        .header("X-ROLE", "DRIVER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId").value(10))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.seatsTotal").value(3))
                .andExpect(jsonPath("$.seatsAvailable").value(3));
    }

    @Test
    void createTripFailsIfNotDriver() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "11")
                        .header("X-ROLE", "PASSENGER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only users with DRIVER role can perform this action"));
    }

    @Test
    void listTripsReturnsCreatedTrip() throws Exception {
        mockMvc.perform(post("/api/carpooling/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "42")
                        .header("X-ROLE", "DRIVER")
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
                        .header("X-ROLE", "DRIVER")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long tripId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/carpooling/trips/{id}/cancel", tripId)
                        .header("X-USER-ID", "999")
                        .header("X-ROLE", "DRIVER"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/carpooling/trips/{id}/cancel", tripId)
                        .header("X-USER-ID", "100")
                        .header("X-ROLE", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
