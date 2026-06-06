package org.jas.ksinxapp.payment;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.models.*;
import com.paypal.sdk.http.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.PaymentResponse;
import org.jas.ksinxapp.model.Course;
import org.jas.ksinxapp.model.PaymentStatus;
import org.jas.ksinxapp.model.PaymentTransaction;
import org.jas.ksinxapp.repo.CourseRepo;
import org.jas.ksinxapp.repo.PaymentTransactionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaypalServerSdkClient paypalClient;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final CourseRepo courseRepo;

    @Transactional
    public CompletableFuture<PaymentResponse> createOrder(Long userId, Long courseId) {

        // Fetch real price from DB — never trust the frontend
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Build the PayPal order request
        OrderRequest orderRequest = new OrderRequest.Builder(
                CheckoutPaymentIntent.CAPTURE,
                List.of(
                        new PurchaseUnitRequest.Builder(
                                new AmountWithBreakdown.Builder(
                                        "USD",
                                        course.getPrice().toPlainString()
                                ).build()
                        )
                                .referenceId("COURSE_" + courseId)
                                .build()
                )
        )
                .applicationContext(
                        new OrderApplicationContext.Builder()
                                .brandName("LutorLMS")
                                .userAction(OrderApplicationContextUserAction.PAY_NOW)
                                .returnUrl("http://localhost:8080/api/v1/payments/return")
                                .cancelUrl("http://localhost:8080/api/v1/payments/cancel")
                                .build()
                )
                .build();

        return paypalClient.getOrdersController()
                .createOrderAsync(
                        new CreateOrderInput.Builder(null, orderRequest)
                                .prefer("return=representation")
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .thenApply(order -> {
                    // Save PENDING transaction to DB
                    PaymentTransaction transaction = PaymentTransaction.builder()
                            .paypalOrderId(order.getId())
                            .userId(userId)
                            .courseId(courseId)
                            .amount(course.getPrice())
                            .currency("USD")
                            .status(PaymentStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build();
                    paymentTransactionRepo.save(transaction);

                    // Extract approval link from PayPal response
                    String approvalLink = order.getLinks().stream()
                            .filter(link -> "approve".equals(link.getRel()))
                            .map(LinkDescription::getHref)
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No approval link from PayPal"));

                    log.info("PayPal order created: {}", order.getId());

                    return PaymentResponse.builder()
                            .paypalOrderId(order.getId())
                            .approvalLink(approvalLink)
                            .status(PaymentStatus.PENDING)
                            .amount(course.getPrice())
                            .currency("USD")
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .exceptionally(ex -> {
                    log.error("Failed to create PayPal order", ex);
                    throw new RuntimeException("Payment creation failed: " + ex.getMessage());
                });
    }

    @Transactional
    public CompletableFuture<PaymentResponse> captureOrder(String paypalOrderId, Long userId) {
        return paypalClient.getOrdersController()
                .captureOrderAsync(
                        new CaptureOrderInput.Builder(paypalOrderId, null)
                                .prefer("return=representation")
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .thenApply(order -> {
                    // Find the PENDING transaction saved in createOrder()
                    PaymentTransaction transaction = paymentTransactionRepo
                            .findByPaypalOrderId(paypalOrderId)
                            .orElseThrow(() -> new RuntimeException("Transaction not found"));

                    // Extract capture ID from nested PayPal response
                    String captureId = order.getPurchaseUnits().get(0)
                            .getPayments().getCaptures().get(0).getId();

                    // Update transaction in DB
                    transaction.setStatus(PaymentStatus.COMPLETED);
                    transaction.setPaypalCaptureId(captureId);
                    transaction.setCompletedAt(LocalDateTime.now());
                    paymentTransactionRepo.save(transaction);

                    log.info("PayPal order captured: {} captureId: {}", paypalOrderId, captureId);

                    return PaymentResponse.builder()
                            .paypalOrderId(paypalOrderId)
                            .status(PaymentStatus.COMPLETED)
                            .amount(transaction.getAmount())
                            .currency(transaction.getCurrency())
                            .build();
                })
                .exceptionally(ex -> {
                    log.error("Failed to capture PayPal order", ex);
                    throw new RuntimeException("Payment capture failed: " + ex.getMessage());
                });
    }
}