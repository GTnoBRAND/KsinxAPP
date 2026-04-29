package org.jas.ksinxapp.model;

import com.paypal.sdk.models.PaymentTokenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String paypalOrderId;
    @Column(nullable = false)
    private String payPalCaptureId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long courseId;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;
    private String notes;
    @Version
    private Long version;


    public PaymentTransaction() {
    }

    public PaymentTransaction(Long id, String paypalOrderId, String payPalCaptureId, Long userId, Long courseId, BigDecimal amount, String currency, PaymentStatus status, LocalDateTime createdAt, LocalDateTime completedAt, String failureReason, String notes, Long version) {
        this.id = id;
        this.paypalOrderId = paypalOrderId;
        this.payPalCaptureId = payPalCaptureId;
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
        this.notes = notes;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String getPayPalCaptureId() {
        return payPalCaptureId;
    }

    public void setPayPalCaptureId(String payPalCaptureId) {
        this.payPalCaptureId = payPalCaptureId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
