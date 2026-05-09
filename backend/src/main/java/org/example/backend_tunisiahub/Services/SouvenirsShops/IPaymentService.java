package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CreatePaymentRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;

public interface IPaymentService {

    List<Payment> retrieveAllPayments();

    Payment retrievePayment(Long id);

    List<Payment> retrievePaymentsByOrder(Long orderId);

    Payment addPayment(CreatePaymentRequest request);

    void deletePayment(Long id);

    Payment modifyPayment(Payment payment);
}
