package org.jas.ksinxapp.payment;


import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.PaymentStatus;
import org.jas.ksinxapp.model.PaymentTransaction;
import org.jas.ksinxapp.repo.PaymentTransactionRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PayPalPaymentService {

    private final PaypalServerSdkClient paypalServerSdkClient;
    private final PaymentTransactionRepo paymentTransactionRepo;

    public PayPalPaymentService(PaypalServerSdkClient paypalServerSdkClient, PaymentTransactionRepo paymentTransactionRepo) {
        this.paypalServerSdkClient = paypalServerSdkClient;
        this.paymentTransactionRepo = paymentTransactionRepo;
    }

    //gte captured payment details
    public CompletableFuture<CapturedPayment> getCapturedPaymnet(String captureId){
        return paypalServerSdkClient.getPaymentsController()
                .getCapturedPaymentAsync(
                        new GetCapturedPaymentInput.Builder(captureId)
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .exceptionally(throwable -> {
                    log.error("Error retrieving captured payment: {}", captureId, throwable);
                    throw new RuntimeException("Failed to retrieve captured payment", throwable);
                });
    }

    //refund captured payment(partial)
    @Transactional
    public CompletableFuture<Refund> refundPayment(String captureId, BigDecimal amount, String currency){
        log.info("Refunding Payment - Capture Id: {}, Amount: {}", captureId, amount);

        RefundRequest refundRequest = new RefundRequest.Builder()
                .amount(new Money(currency, amount.toPlainString()))
                .build();

        return paypalServerSdkClient.getPaymentsController()
                .refundCapturedPaymentAsync(
                        new RefundCapturedPaymentInput.Builder(captureId, null)
                                .prefer("return=representastion")
                                .body(refundRequest)
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .thenApply(refund -> {
                    //update transaction
                    PaymentTransaction paymentTransaction = paymentTransactionRepo.findByPayPalCaptureId(captureId)
                            .orElseThrow(()-> new RuntimeException("Payment not found"));

                    paymentTransaction.setStatus(PaymentStatus.REFUNDED);
                    paymentTransactionRepo.save(paymentTransaction);

                    log.info("Payment refunded: {}", refund.getId());
                    return refund;
                })
                .exceptionally(throwable -> {
                    log.error("Error refunding payment: {}", captureId, throwable);
                    throw new RuntimeException("Failed to refund payment", throwable);
                });
    }

    //full refund
    @Transactional
    public CompletableFuture<Refund> getFullRefund(String captureId){
        log.info("Processing full refund for capture: {}", captureId);

        return paypalServerSdkClient.getPaymentsController()
                .refundCapturedPaymentAsync(
                        new RefundCapturedPaymentInput.Builder(captureId, null)
                                .prefer("return=representation")
                                .build()
                ).thenApply(ApiResponse::getResult)
                .thenApply(refund -> {
                    PaymentTransaction paymentTransaction = paymentTransactionRepo.findByPayPalCaptureId(captureId)
                            .orElseThrow(()->new RuntimeException("Payment not found"));
                    paymentTransaction.setStatus(PaymentStatus.REFUNDED);
                    paymentTransactionRepo.save(paymentTransaction);

                    log.info("Full refund processed: {}", refund.getId());
                    return refund;
                }).exceptionally(throwable -> {
                    log.error("Error processing full refund: {}", captureId, throwable);
                    throw new RuntimeException("Failed to process refund", throwable);
                });
    }

    //get refund details
    public CompletableFuture<Refund> getRefund(String refundId){
        return paypalServerSdkClient.getPaymentsController()
                .getRefundAsync(new GetRefundInput.Builder(refundId).build())
                .thenApply(ApiResponse::getResult)
                .exceptionally(throwable -> {
                    log.error("Error retrieving refund: {}", refundId, throwable);
                    throw new RuntimeException("Failed to retrieve refund", throwable);
                });
    }
}
