package com.example.demo;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private PaymentService paymentService;


    public void placeOrder() {


            var paymentService = new StripePaymentService();
            paymentService.processPayment(10);
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
