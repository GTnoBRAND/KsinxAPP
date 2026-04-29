package org.jas.ksinxapp.payment;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jas.ksinxapp.model.SubscriptionStatus;
import org.jas.ksinxapp.repo.PaymentTransactionRepo;
import org.jas.ksinxapp.model.SubscriptionModel;
import org.jas.ksinxapp.repo.SubscriptionRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PayPalSubscriptionService {

    private final PaypalServerSdkClient paypalServerSdkClient;
    private final SubscriptionRepo subscriptionRepo;

    public PayPalSubscriptionService(PaypalServerSdkClient paypalServerSdkClient, SubscriptionRepo subscriptionRepo) {
        this.paypalServerSdkClient = paypalServerSdkClient;
        this.subscriptionRepo = subscriptionRepo;
    }


    //create a billing plan

    @Transactional
    public CompletableFuture<String> createBillingPlan(
            String planName,
            String productId,
            String amount,
            String currency,
            String interval
    ){
        log.info("Creating a billing plan: {}", planName);

        IntervalUnit intervalUnit = IntervalUnit.valueOf(interval.toUpperCase());

        PlanRequest planRequest = new PlanRequest.Builder()
                .productId(productId)
                .name(planName)
                .billingCycles(
                Arrays.asList(
                        new SubscriptionBillingCycle.Builder()
                                .frequency(
                                new Frequency.Builder()
                                        .intervalUnit(intervalUnit)
                                        .intervalCount(1)
                                        .build()
                                        )
                                .tenureType(TenureType.REGULAR)
                                .totalCycles(0) //0infinite cycles
                                .pricingScheme(
                                        new SubscriptionPricingScheme.Builder()
                                                .fixedPrice(new Money.Builder()
                                                        .currencyCode(currency)
                                                        .value(amount)
                                                        .build())
                                                .build()
                                )
                                .build()

                )
        ).status(PlanRequestStatus.ACTIVE)
                .paymentPreferences(
                        new PaymentPreferences.Builder()
                                .autoBillOutstanding(true)
                                .setupFeeFailureAction(SetupFeeFailureAction.CANCEL)
                                .paymentFailureThreshold(3)
                                .build()
                ).build();
        return paypalServerSdkClient.getSubscriptionsController()
                .createBillingPlanAsync(
                        new CreateBillingPlanInput.Builder()
                                .contentType(null)
                                .prefer("return=representation")
                                .body(planRequest)
                                //.paypalRequestId()
                                .build()
                )
                .thenApply(ApiResponse::getResult)
                .thenApply(billingPlan -> {
                    log.info("Billing plan created: {}", billingPlan.getId());
                    return billingPlan.getId();
                })
                .exceptionally(throwable -> {
                    log.error("Error creating billing plan", throwable);
                    throw new RuntimeException("Failed to create a billing plan", throwable);
                });

    }

    //create a subscription
    @Transactional
    public CompletableFuture<SubscriptionModel> createSubscription(
            Long userId,
            String planId,
            String subscribeName,
            String subscribeEmail,
            String amount,
            String currency
    ){
        log.info("Creating subscription for user: {} plan: {}", userId, planId);

        CreateSubscriptionRequest createSubscriptionRequest = new CreateSubscriptionRequest.Builder(planId)
                .subscriber(
                        new SubscriberRequest.Builder()
                                .name(new Name.Builder().givenName(subscribeName).build())
                                .emailAddress(subscribeEmail)
                                .build()
                ).autoRenewal(true)
                .build();
        return paypalServerSdkClient.getSubscriptionsController()
                .createSubscriptionAsync(
                        new CreateSubscriptionInput.Builder(null)
                                .prefer("return=presentation")
                                .body(createSubscriptionRequest)
                                .build()
                ).thenApply(ApiResponse::getResult)
                .thenApply(subscription -> {
                    //save to db
                    SubscriptionModel subscriptionModel = SubscriptionModel.builder()
                            .paypalSubscriptionId(subscription.getId())
                            .paypalPlanId(planId)
                            .userId(userId)
                            .amount(new java.math.BigDecimal(amount))
                            .currency(currency)
                            .status(SubscriptionStatus.APPROVAL_PENDING)
                            .createdAt(LocalDateTime.now())
                            .build();

                    subscriptionRepo.save(subscriptionModel);
                    log.info("Subscription created: {}", subscription.getId());
                    return subscriptionModel;

                })
                .exceptionally(throwable -> {
                    log.error("Error creating subscription", throwable);
                    throw new RuntimeException("Failed to create subscription", throwable);
                });



    }

    //get subscription details

    public CompletableFuture<SubscriptionModel> getSubscription(String subscriptionId){
        return paypalServerSdkClient.getSubscriptionsController()
                .getSubscriptionAsync(
                        new GetSubscriptionInput.Builder(subscriptionId).build()
                ).thenApply(ApiResponse::getResult)
                .thenApply(subscription -> {
                    SubscriptionModel subscriptionModel = subscriptionRepo.findByPaypalSubscriptionId(subscriptionId)
                            .orElseThrow(()->new RuntimeException("Subscription not found!"));
                    return subscriptionModel;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving subscription: {}", subscriptionId, throwable);
                    throw new RuntimeException("Failed to retrieve subscription", throwable);
                });

    }

    //cancel subscription

    @Transactional
    public CompletableFuture<Void> cancelSubscription(String subscriptionId, String reason){
        log.info("Cancel subscription: {}", subscriptionId);

        CancelSubscriptionRequest cancelSubscriptionRequest = new CancelSubscriptionRequest(reason);

        return paypalServerSdkClient.getSubscriptionsController()
                .cancelSubscriptionAsync(
                        new CancelSubscriptionInput.Builder(subscriptionId, null)
                                .body(cancelSubscriptionRequest)
                                .build()
                )
                .thenAccept(v->{
                    //update  database
                    SubscriptionModel subscriptionModel = subscriptionRepo.findByPaypalSubscriptionId(subscriptionId)
                            .orElseThrow(()->new RuntimeException("Subscription not found"));

                    subscriptionModel.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionModel.setCanceledAt(LocalDateTime.now());
                    subscriptionModel.setCancellationReason(reason);
                    subscriptionRepo.save(subscriptionModel);

                    log.info("Subscription canceled: {}", subscriptionId);
                })
                .exceptionally(throwable -> {
                    log.error("Error canceling subscription: {}", subscriptionId, throwable);
                    throw new RuntimeException("Failed to cancel subscription", throwable);
                });
    }

}