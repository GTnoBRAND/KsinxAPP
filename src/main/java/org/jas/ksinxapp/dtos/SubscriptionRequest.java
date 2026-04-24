package org.jas.ksinxapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequest {

    private String planName;
    private String planDescription;
    private BigDecimal amount;
    private String currency;
    private String interval;    //month, year
}
