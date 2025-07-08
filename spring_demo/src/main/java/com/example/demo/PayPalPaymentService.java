package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public  class PayPalPaymentService implements PaymentService {

    @Override
    public void processPayment(double amount) {
        System.out.println("Paypal");
        System.out.println("Amount" + amount);
    }
}
