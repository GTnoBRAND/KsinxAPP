package org.jas.ksinxapp.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.PaymentStatus;
import org.jas.ksinxapp.model.SubscriptionStatus;
import org.jas.ksinxapp.repo.PaymentTransactionRepo;
import org.jas.ksinxapp.repo.SubscriptionRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayPalWebHookService {

    private final SubscriptionRepo subscriptionRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ObjectMapper objectMapper;

    //handle PayPal webhook events
    @Transactional
    public void handleWebHookEvent(String eventBody, String eventType) throws JsonProcessingException {

        log.info("Processing webhook event type: {}", eventType);


            JsonNode event = objectMapper.readTree(eventBody);


        try {
            switch (eventType) {
                case "CHECKOUT.ORDER.COMPLETED":
                    handleOrderCompleted(event);
                    break;
                case "PAYMENT.CAPTURE.COMPLETED":
                    handlePaymentCompleted(event);
                    break;
                case "PAYMENT.CAPTURE.REFUNDED":
                    handlePaymentRefunded(event);
                    break;
                case "BILLING.SUBSCRIPTION.CREATED":
                    handleSubscriptionCreated(event);
                    break;
                case "BILLING.SUBSCRIPTION.UPDATED":
                    handleSubscriptionUpdated(event);
                    break;
                case "BILLING.SUBSCRIPTION.CANCELLED":
                    handleSubscriptionCancelled(event);
                    break;
                default:
                    log.warn("Unhandled webhook event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing webhook event", e);
            throw new RuntimeException("Failed to process webhook", e);
        }

    }

    private void handleOrderCompleted(JsonNode event) {
        String orderID = event.get("resource").get("id").asText();
        String status = event.get("resource").get("status").asText();
        log.info("Order completed - ID: {}, status: {}", orderID, status);


        //update payment transaction
        paymentTransactionRepo.findByPaypalOrderId(orderID).ifPresent(paymentTransaction -> {
            paymentTransaction.setStatus(PaymentStatus.COMPLETED);
            paymentTransaction.setCompletedAt(LocalDateTime.now());
            paymentTransactionRepo.save(paymentTransaction);
            log.info("Payment transaction updated");
        });
    }

    private void handleSubscriptionUpdated(JsonNode event) {
        String subscriptionId = event.get("resource").get("id").asText();
        String status = event.get("resource").get("status").asText();

        subscriptionRepo.findByPaypalSubscriptionId(subscriptionId).ifPresent(sub -> {
            try {
                // toUpperCase() helps avoid case-sensitivity issues
                sub.setStatus(SubscriptionStatus.valueOf(status.toUpperCase()));
                subscriptionRepo.save(sub);
                log.info("Subscription {} status updated to {}", subscriptionId, status);
            } catch (IllegalArgumentException e) {
                log.error("Unknown subscription status received: {}", status);
            }
        });
    }

    private void handleSubscriptionCreated(JsonNode event) {
        String subscriptionId = event.get("resource").get("id").asText();
        String status = event.get("resource").get("status").asText();
        log.info("Subscription created - ID: {}, status: {}", subscriptionId, status);

        subscriptionRepo.findByPaypalSubscriptionId(subscriptionId).ifPresent(subscriptionModel -> {
            subscriptionModel.setStatus(SubscriptionStatus.valueOf(status.toUpperCase()));
            subscriptionRepo.save(subscriptionModel);
        });
    }

    private void handlePaymentRefunded(JsonNode event) {
        JsonNode resource = event.get("resource");
        String refundId = resource.get("id").asText();

        if (resource.has("links")) {
            for (JsonNode link : resource.get("links")) {
                if ("up".equals(link.get("rel").asText())) {
                    // FIXED: Replaced .pop with proper array indexing
                    String href = link.get("href").asText();
                    String[] parts = href.split("/");
                    String captureId = parts[parts.length - 1];

                    paymentTransactionRepo.findByPayPalCaptureId(captureId).ifPresent(tx -> {
                        tx.setStatus(PaymentStatus.REFUNDED);
                        paymentTransactionRepo.save(tx);
                        log.info("Transaction for capture {} marked as REFUNDED", captureId);
                    });
                    break;
                }
            }
        }
    }

    private void handlePaymentCompleted(JsonNode event) {
        String captureId = event.get("resource").get("id").asText();
        String status = event.get("resource").get("status").asText();
        String amount = event.get("resource").get("amount").get("value").asText();
        log.info("Payment completed -Capture ID: {}, status: {}, Amount: {}", captureId, status, amount);

        paymentTransactionRepo.findByPayPalCaptureId(captureId).ifPresent(paymentTransaction -> {
            paymentTransaction.setStatus(PaymentStatus.COMPLETED);
            paymentTransaction.setCompletedAt(LocalDateTime.now());
            paymentTransactionRepo.save(paymentTransaction);
        });
    }

    private void handleSubscriptionCancelled(JsonNode event) {
        String subscriptionId = event.get("resource").get("id").asText();
        log.info("Subscription cancelled: {}", subscriptionId);

        subscriptionRepo.findByPaypalSubscriptionId(subscriptionId).ifPresent(subscriptionModel -> {
            subscriptionModel.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionModel.setCanceledAt(LocalDateTime.now());
            subscriptionRepo.save(subscriptionModel);
        });

    }
}
