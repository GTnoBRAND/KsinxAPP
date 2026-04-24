package org.jas.ksinxapp.payment;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.dtos.CreatePaymentRequest;
import org.jas.ksinxapp.dtos.PaymentResponse;
import org.jas.ksinxapp.model.PaymentStatus;
import org.jas.ksinxapp.model.PaymentTransaction;
import org.jas.ksinxapp.repo.PaymentTransactionRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaypalServerSdkClient paypalServerSdkClient;
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Value("${app.base-uri}")
    private String baseUrl;


    //create the order for course payment
    @Transactional
    public CompletableFuture<PaymentResponse> createCoursePayment(Long userId, CreatePaymentRequest request){
        log.info("Creating PayPal order for user: {}, course: {}", userId, request.getCourseId());

        //build the order request
        OrderRequest orderRequest = new OrderRequest.Builder(
                CheckoutPaymentIntent.CAPTURE,
                Arrays.asList(
                        new PurchaseUnitRequest.Builder(
                                new AmountWithBreakdown.Builder(
                                        request.getCurrency(),
                                        request.getAmount().toPlainString()
                                )
                                        .breakdown(
                                                new AmountBreakdown.Builder()
                                                        .itemTotal(new Money.Builder()
                                                                .currencyCode(request.getCurrency())
                                                                .value(request.getAmount().toPlainString())
                                                                .build() // This builds the Money object
                                                        )
                                                        .build() // <--- ADD THIS: This builds the AmountBreakdown object
                                        )
                                        .build()
                        )
                                .referenceId("COURSE_" + request.getCourseId())
                                .items(Arrays.asList(
                                       new ItemRequest.Builder()
                                               .name(request.getCourseName())
                                               .quantity("1")
                                               .description(request.getCourseDescription())
                                               .unitAmount(new Money.Builder()
                                                       .currencyCode(request.getCurrency())
                                                       .value(request.getAmount().toPlainString())
                                                       .build())
                                               .build()

                                ))
                                .build()
                )

        )
                .applicationContext(
                        new OrderApplicationContext.Builder()
                                .brandName("KsinxAPP language Learning Center")
                                .landingPage(OrderApplicationContextLandingPage.BILLING)
                                .userAction(OrderApplicationContextUserAction.PAY_NOW)
                                .returnUrl(baseUrl + "/payments/return?userId=" + userId + "&courseId=" + request.getCourseId())
                                .cancelUrl(baseUrl + "/payments/cancel")
                                .build()
                ).build();
        //create order in paypal
        return paypalServerSdkClient.getOrdersController()
                .createOrderAsync(
                        new CreateOrderInput.Builder(null, orderRequest)
                                .prefer("return=representation")
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .thenApply(order -> {
                    //save pending transaction to db
                    PaymentTransaction transaction = PaymentTransaction.builder()
                            .paypalOrderId(order.getId())
                            .userId(userId)
                            .courseId(request.getCourseId())
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .status(PaymentStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build();
                    paymentTransactionRepo.save(transaction);

                    //extract approval link
                    String approvalLink = order.getLinks().stream()
                            .filter(link->"approve".equals(link.getRel()))
                            .map(LinkDescription::getHref)
                            .findFirst()
                            .orElse(null);

                    log.info("Order created successfully: {}", order.getId());

                    return PaymentResponse.builder()
                            .paypalOrderId(order.getId())
                            .status(order.getStatus().toString())
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .createdAt(LocalDateTime.now())
                            .approvalLink(approvalLink)
                            .build();
                })
                .exceptionally(throwable -> {
                    log.error("Error creating PayPal order", throwable);
                    throw new RuntimeException("Failed to create order: " + throwable.getMessage(), throwable);
                });

    }

    //capture order after user approval

    @Transactional
    public CompletableFuture<PaymentResponse> captureOrder(String orderId, Long userId){
        log.info("Capturing an order: {} for user: {}", orderId, userId);

        return paypalServerSdkClient.getOrdersController()
                .createOrderAsync(
                        new CreateOrderInput.Builder()
                                .prefer("return=representation")
                                .build()
                ).thenApply(ApiResponse::getResult)
                .thenApply(order -> {
                    //update transaction status
                    PaymentTransaction transaction = paymentTransactionRepo.findByPaypalOrderId(orderId)
                            .orElseThrow(()->new RuntimeException("Transaction not found"));

                    //extract capture ID
                    String captureId = order.getPurchaseUnits().get(0)
                            .getPayments().getCaptures().get(0).getId();

                    transaction.setPayPalCaptureId(captureId);
                    transaction.setStatus(PaymentStatus.COMPLETED);
                    transaction.setCompletedAt(LocalDateTime.now());
                    paymentTransactionRepo.save(transaction);

                    log.info("Order captured successfully: {}", orderId);

                    return PaymentResponse.builder()
                            .paypalOrderId(orderId)
                            .status("COMPLETED")
                            .amount(transaction.getAmount())
                            .currency(transaction.getCurrency())
                            .createdAt(transaction.getCreatedAt())
                            .build();
                })
                .exceptionally(throwable -> {
                    log.error("Error capturing order: {}", orderId, throwable);
                    throw new RuntimeException("Failed to capture order", throwable);
                });

    }

    //get order details
    public CompletableFuture<Order> getOrderDetails(String orderId){
        return paypalServerSdkClient.getOrdersController()
                .getOrderAsync(new GetOrderInput.Builder(orderId).build())
                .thenApply(ApiResponse::getResult)
                .exceptionally(throwable -> {
                    log.error("Error retrieving order: {}", orderId, throwable);
                    throw new RuntimeException("Failed to retrieve order", throwable);
                });
    }

}
