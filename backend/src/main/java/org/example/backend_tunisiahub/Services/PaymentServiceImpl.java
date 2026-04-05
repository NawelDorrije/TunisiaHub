package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.PaymentMapper;
import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Entities.PaymentStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Repositories.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final IReservationService reservationService;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ReservationRepository reservationRepository,
                              IReservationService reservationService,
                              PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentDTO processPayment(Long reservationId, PaymentMethod method) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        // Only PENDING reservations can be paid
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not in PENDING state");
        }

        // Prevent double payment
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            throw new RuntimeException("Payment already exists for this reservation");
        }

        // Simulate payment processing
        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(reservation.getTotalPrice())
                .method(method)
                .status(PaymentStatus.SUCCESS)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Automatically update reservation status to PAID
        reservationService.updateStatus(reservationId, ReservationStatus.PAID);

        return paymentMapper.toDTO(payment);
    }

    @Override
    public PaymentDTO refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        reservationService.updateStatus(payment.getReservation().getId(), ReservationStatus.CANCELLED);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentDTO getByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(paymentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("No payment found for reservation: " + reservationId));
    }

    @Override
    public PaymentDTO getById(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    @Override
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO).collect(Collectors.toList());
    }
}