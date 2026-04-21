package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<Reservation> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation retrieveReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
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

    @Override
    public Reservation reserveEvent(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (false) {
            throw new RuntimeException("Event capacity not defined");
        }

        if (reservationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new RuntimeException("User already reserved this event");
        }


        long count = reservationRepository.countByEventId(eventId);

        if (count >= event.getCapacity()) {
            event.setStatus("COMPLETED");
            eventRepository.save(event);
            throw new RuntimeException("Event is FULL");
        }

        Reservation r = new Reservation();
        r.setUser(user);
        r.setEvent(event);
        r.setStatus("PENDING"); // important
        r.setTotalPrice(event.getPrice());
        r.setType(ReservationType.EventReservation);

        return reservationRepository.save(r);
    }
    @Override
    public Reservation createPendingReservation(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Reservation existing = reservationRepository
                .findByUserIdAndEventId(userId, eventId);

        if (existing != null) {
            // 🔥 IMPORTANT FIX
            if ("CANCELLED".equals(existing.getStatus())) {
                existing.setStatus("PENDING");
                return reservationRepository.save(existing);
            }
            return existing;
        }

        Reservation r = new Reservation();
        r.setUser(user);
        r.setEvent(event);
        r.setStatus("PENDING");
        r.setTotalPrice(event.getPrice());
        r.setType(ReservationType.EventReservation);

        return reservationRepository.save(r);
    }

    @Override
    public Reservation confirmReservation(Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setStatus("CONFIRMED");

        return reservationRepository.save(r);
    }

    @Override
    public Reservation getUserReservationForEvent(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Reservation reservation = reservationRepository.findByUserIdAndEventId(userId, eventId);

        if (reservation == null) {
            throw new RuntimeException("Reservation not found for this user and event");
        }

        return reservation;
    }
    @Override
    public Reservation findByUserAndEvent(Long userId, Long eventId) {
        return reservationRepository.findByUserIdAndEventId(userId, eventId);
    }



}
