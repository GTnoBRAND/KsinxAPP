package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "subscriptions")
@Builder
public class SubscriptionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paypalSubscriptionId;
    private String paypalPlanId;
    private Long userId;
    private BigDecimal amount;
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime nextBillingDate;
    private LocalDateTime canceledAt;

    private String cancellationReason;

    @Version
    private Long version;


    public SubscriptionModel() {
    }

    public SubscriptionModel(Long id, String paypalSubscriptionId, String paypalPlanId, Long userId, BigDecimal amount, String currency, SubscriptionStatus status, LocalDateTime createdAt, LocalDateTime nextBillingDate, LocalDateTime canceledAt, String cancellationReason, Long version) {
        this.id = id;
        this.paypalSubscriptionId = paypalSubscriptionId;
        this.paypalPlanId = paypalPlanId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.nextBillingDate = nextBillingDate;
        this.canceledAt = canceledAt;
        this.cancellationReason = cancellationReason;
        this.version = version;
    }

}
