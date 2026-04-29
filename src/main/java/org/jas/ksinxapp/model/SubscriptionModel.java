package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaypalSubscriptionId() {
        return paypalSubscriptionId;
    }

    public void setPaypalSubscriptionId(String paypalSubscriptionId) {
        this.paypalSubscriptionId = paypalSubscriptionId;
    }

    public String getPaypalPlanId() {
        return paypalPlanId;
    }

    public void setPaypalPlanId(String paypalPlanId) {
        this.paypalPlanId = paypalPlanId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
