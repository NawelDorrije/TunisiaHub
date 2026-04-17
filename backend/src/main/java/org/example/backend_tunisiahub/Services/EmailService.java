package org.example.backend_tunisiahub.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPaymentEmail(String toEmail, String userName, double amount) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Payment Confirmation - TunisiaHub");

        message.setText(
                "Hello " + userName + ",\n\n" +
                        "✅ Your payment was successful!\n\n" +
                        "💰 Amount: " + amount + " TND\n\n" +
                        "Thank you for using TunisiaHub 🎉\n\n" +
                        "Best regards,\nTunisiaHub Team"
        );

        message.setFrom("sirineselmi012@gmail.com");

        mailSender.send(message);
    }
}