package org.jas.ksinxapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.CreatePaymentRequest;
import org.jas.ksinxapp.dtos.PaymentResponse;
import org.jas.ksinxapp.payment.PaymentService;
import org.jas.ksinxapp.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<PaymentResponse>> createOrder(
            Authentication auth,
            @Valid @RequestBody CreatePaymentRequest request) {

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long userId = principal.getUser().getId();

        return paymentService.createOrder(userId, request.courseId())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Error creating PayPal order", ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    @GetMapping("/return")
    public CompletableFuture<ResponseEntity<PaymentResponse>> handleReturn(
            @RequestParam String token,
            Authentication auth) {

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long userId = principal.getUser().getId();

        return paymentService.captureOrder(token, userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Error capturing PayPal order", ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handleCancel() {
        return ResponseEntity.ok("Payment cancelled");
    }
}