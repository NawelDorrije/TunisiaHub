package org.example.backend_tunisiahub.Controllers.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Payment;
import org.example.backend_tunisiahub.Services.Event.IPaymentService;
import org.example.backend_tunisiahub.dto.StripePaymentRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @GetMapping("/all")
    public List<Payment> getAll() {
        return paymentService.retrieveAllPayments();
    }

   // @PostMapping("/pay")
    //public Payment pay(@RequestParam Long reservationId,
      //                 @RequestParam double amount) {
        //return paymentService.pay(reservationId, amount);
    //}
   @PostMapping("/pay")
   public Map<String, Object> pay(@RequestBody StripePaymentRequest request) {

       if (request == null || request.getReservationId() == null) {
           throw new RuntimeException("reservationId is NULL ❌");
       }

       Payment payment = paymentService.pay(
               request.getReservationId(),
               request.getAmount()
       );

       return Map.of(
               "status", "success",
               "paymentId", payment.getId(),
               "amount", payment.getAmount()
       );
   }


    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
}
