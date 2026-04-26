package org.jas.ksinxapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
