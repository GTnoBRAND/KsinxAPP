package org.jas.ksinxapp.controller;

import org.jas.ksinxapp.dtos.CreatePaymentRequest;
import org.jas.ksinxapp.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @GetMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentRequest request){
         return ResponseEntity.ok(paymentService.createPayment(request));
    }
}
