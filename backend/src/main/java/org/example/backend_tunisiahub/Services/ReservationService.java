package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Camping.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;
    @Override
    public List<Reservation> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation retrieveReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation addReservationCamping(ReservationDTO dto) {

        // Vérifier si ce spot est déjà réservé par cet utilisateur
        boolean alreadyReserved = reservationRepository.existsByUserIdAndSpotId(dto.userId, dto.spotId);
        if (alreadyReserved) {
            throw new RuntimeException("Ce spot est déjà réservé par cet utilisateur");
        }

        Reservation reservation = new Reservation();

        reservation.setStartDateCamping(dto.startDateCamping);
        reservation.setEndDateCamping(dto.endDateCamping);
        reservation.setNumberOfPeopleCamping(dto.numberOfPeopleCamping);
        reservation.setTotalPriceCamping(dto.totalPriceCamping);

        reservation.setStatusCamping(
                ReservationStatus.valueOf(dto.statusCamping)
        );

        User user = userRepository
                .findById(dto.userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Spot spot = spotRepository
                .findById(dto.spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        reservation.setUser(user);
        reservation.setSpot(spot);

        return reservationRepository.save(reservation);
    }
    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public Reservation modifyReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
}
