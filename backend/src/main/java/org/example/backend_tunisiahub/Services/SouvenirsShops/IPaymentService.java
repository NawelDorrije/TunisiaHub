package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;

public interface IPaymentService {

    List<Payment> retrieveAllPayments();

    Payment retrievePayment(Long id);

    Payment retrievePaymentByOrder(Long orderId);

    Payment addPayment(Payment payment);

    void deletePayment(Long id);

    Payment modifyPayment(Payment payment);
}
