package org.jas.ksinxapp.payment;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import com.paypal.sdk.Environment;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PaymentConfig {

    @Value("${paypal.client.id:}")
    private String clientId;

    @Value("${paypal.client.secret:}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    @Bean
    public PaypalServerSdkClient paypalClient() {
        Environment environment = "production".equalsIgnoreCase(mode)
                ? Environment.PRODUCTION
                : Environment.SANDBOX;

        log.info("PayPal client initialized in {} mode", mode);

        return new PaypalServerSdkClient.Builder()
                .environment(environment)
                .clientCredentialsAuth(
                        new ClientCredentialsAuthModel.Builder(clientId, clientSecret)
                                .build()
                )
                .loggingConfig(builder -> builder.level(Level.INFO))
                .build();
    }
}