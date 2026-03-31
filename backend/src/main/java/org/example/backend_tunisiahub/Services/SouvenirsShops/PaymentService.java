package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> retrieveAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment retrievePayment(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Payment retrievePaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public Payment modifyPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
