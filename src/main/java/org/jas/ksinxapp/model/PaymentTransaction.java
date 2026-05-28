package org.jas.ksinxapp.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
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

}
