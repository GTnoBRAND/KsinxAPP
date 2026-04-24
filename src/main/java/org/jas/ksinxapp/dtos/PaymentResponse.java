package org.jas.ksinxapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    private String paypalOrderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private String approvalLink;
}
