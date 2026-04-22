package org.jas.ksinxapp.payment;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentConfig {

    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public PaypalServerSdkClient paypalServerSdkClient(){
        return new PaypalServerSdkClient.Builder()
                .loggingConfig(builder -> builder.level(Level.INFO))
                .environment(mode.equalsIgnoreCase("live")
                        ? Environment.PRODUCTION
                        : Environment.SANDBOX)
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(clientId, clientSecret)
                        .build())
                .build();
    }


}
