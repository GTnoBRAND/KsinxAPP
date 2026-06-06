package org.jas.ksinxapp.dtos;

import lombok.*;
import org.jas.ksinxapp.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String paypalOrderId;
    private String approvalLink;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
}