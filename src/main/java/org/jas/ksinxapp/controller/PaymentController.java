package org.jas.ksinxapp.controller;

import com.paypal.sdk.models.Refund;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.CreatePaymentRequest;
import org.jas.ksinxapp.dtos.PaymentResponse;
import org.jas.ksinxapp.model.SubscriptionModel;
import org.jas.ksinxapp.payment.PayPalPaymentService;
import org.jas.ksinxapp.payment.PayPalSubscriptionService;
import org.jas.ksinxapp.payment.PayPalWebHookService;
import org.jas.ksinxapp.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PayPalPaymentService payPalPaymentService;
    private final PayPalWebHookService payPalWebHookService;
    private final PayPalSubscriptionService payPalSubscriptionService;

    public PaymentController(PaymentService paymentService, PayPalPaymentService payPalPaymentService, PayPalWebHookService payPalWebHookService, PayPalSubscriptionService payPalSubscriptionService) {
        this.paymentService = paymentService;
        this.payPalPaymentService = payPalPaymentService;
        this.payPalWebHookService = payPalWebHookService;
        this.payPalSubscriptionService = payPalSubscriptionService;
    }


    // --course payments--

    //create payment order for course enrollment
    @PostMapping("/orders/create")
    public CompletableFuture<ResponseEntity<PaymentResponse>> createCoursePayment(Authentication auth, @RequestBody
                                                                                  CreatePaymentRequest request){
        Long userId = getUserIdFromAuth(auth);
        log.info("Creating payment order for user: {}", userId);

        return paymentService.createCoursePayment(userId, request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex->{
                    log.error("Error creating payment order", ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    //capture order after user approval
    @PostMapping("/orders/{orderId}/capture")
    public CompletableFuture<ResponseEntity<PaymentResponse>> captureOrder(
            Authentication auth,
            @PathVariable String orderId
    ){
        Long userID = getUserIdFromAuth(auth);
        log.info("Capturing order: {} for user: {}", orderId, userID);

        return paymentService.captureOrder(orderId, userID)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex->{
                    log.error("Error capturing payment order", ex);
                    return ResponseEntity.badRequest().build();
                });
    }

    //return url after PayPal approval
    @GetMapping("/return")
    public CompletableFuture<ResponseEntity<String>> handleReturn(
            @RequestParam String tokenId,
            @RequestParam Long userId,
            @RequestParam Long courseId
    ){
        log.info("User {} returned from PayPal for course {}", userId, courseId);

        return paymentService.captureOrder(tokenId, userId)
                .thenApply(paymentResponse -> ResponseEntity.ok("Payment capture successfully"))
                .exceptionally(ex->ResponseEntity.badRequest().body("Payment capture failed"));
    }

    //Return cancel url if user cancels payment
    @GetMapping("/cancel")
    public ResponseEntity<String> handleCancel(){
        log.info("User canceled payment");
        return ResponseEntity.ok("Payment canceled");
    }

    //====REFUNDS====

    /**
     *Refund a payment partial or full
     */
    @PostMapping("/refund/{captureId}")
    public CompletableFuture<ResponseEntity<Object>> refundPayment(
            @PathVariable String captureId,
            @RequestParam String amount,
            @RequestParam String currency
    ){
        CompletableFuture<Refund> refundFuture = (amount != null)
                ? payPalPaymentService.refundPayment(captureId, new BigDecimal(amount), currency)
                : payPalPaymentService.getFullRefund(captureId);

        return refundFuture
                .thenApply(refund -> ResponseEntity.ok((Object) refund))
                .exceptionally(ex->ResponseEntity.badRequest().body("Filed to refund: " + ex.getMessage()));
    }

    /**
     * get refund status
     */
    @GetMapping("/refund/{status}")
    public CompletableFuture<ResponseEntity<Refund>> getRefund(@PathVariable String refundId){
        return payPalPaymentService.getRefund(refundId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex->ResponseEntity.notFound().build());
    }

    //====SUBSCRIPTIONS====

    /**
     * Create subscription plan
     */


    @PostMapping("/subscription/create")
    public CompletableFuture<ResponseEntity<SubscriptionModel>> createSubscription(
            Authentication auth,
            @RequestParam String planID,
            @RequestParam String amount,
            @RequestParam(defaultValue = "USD") String currency
    ){
        Long userID = getUserIdFromAuth(auth);
        String userName = auth.getName();

        return payPalSubscriptionService.createSubscription(
                userID,
                planID,
                userName,
                getEmailFromAuth(auth),
                amount,
                currency
        )
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex->ResponseEntity.badRequest().build());
    }


    /**
     *Cancel subscription
     */
    @PostMapping("/subscription/{subscriptionID}/cancel")
    public CompletableFuture<ResponseEntity<String>> cancelSubscription(
            @PathVariable String subscriptionID,
            @RequestParam(defaultValue = "User requested cancellation") String reason
    )
    {
        return payPalSubscriptionService.cancelSubscription(subscriptionID, reason)
                .thenApply(v->ResponseEntity.ok("Subscription cancelled"))
                .exceptionally(ex->ResponseEntity.badRequest().body("Cancellation failed"));
    }


    //====WEBHOOKS====

    /**
     *
     */

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String body,
            @RequestHeader("PayPal-Transmission-Id") String transmissionId,
            @RequestHeader("PayPal-Transmission-Time") String transmissionTime,
            @RequestHeader("PayPal-Cert-Url") String certUrl,
            @RequestHeader("PayPal-Auth-Algo") String authAlgo,
            @RequestHeader("PayPal-Transmission-Sig") String transmissionSig,
            @RequestHeader("PayPal-Event-Type") String eventType
    ){
        try {
            // TODO: Verify webhook signature in production
            payPalWebHookService.handleWebHookEvent(body, eventType);
            log.info("Webhook processed successfully - Type: {}", eventType);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }


    //====HELPER METHODS====
    private Long getUserIdFromAuth(Authentication auth){
        return Long.parseLong(auth.getPrincipal().toString());
    }

    private String getEmailFromAuth(Authentication auth){
        return auth.getName();
    }

}
