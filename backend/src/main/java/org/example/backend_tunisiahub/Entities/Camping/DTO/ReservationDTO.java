package org.example.backend_tunisiahub.Entities.Camping.DTO;

import java.time.LocalDate;

public class ReservationDTO {
    public LocalDate startDateCamping;

    public LocalDate endDateCamping;

    public int numberOfPeopleCamping;

    public double totalPriceCamping;

    public String statusCamping;

    public Long userId;

    public Long spotId;
}
