package org.jas.ksinxapp.dtos;


import java.math.BigDecimal;
import java.util.Objects;

public class CreatePaymentRequest {
    String amount;
    String currency;
    String intent;
    String description;
    String cancelUrls;
    String successUrls;
    BigDecimal scale;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(String amount, String currency, String intent, String description, String cancelUrls, String successUrls, BigDecimal scale) {
        this.amount = amount;
        this.currency = currency;
        this.intent = intent;
        this.description = description;
        this.cancelUrls = cancelUrls;
        this.successUrls = successUrls;
        this.scale = scale;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCancelUrls() {
        return cancelUrls;
    }

    public void setCancelUrls(String cancelUrls) {
        this.cancelUrls = cancelUrls;
    }

    public String getSuccessUrls() {
        return successUrls;
    }

    public void setSuccessUrls(String successUrls) {
        this.successUrls = successUrls;
    }

    public BigDecimal getScale() {
        return scale;
    }

    public void setScale(BigDecimal scale) {
        this.scale = scale;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CreatePaymentRequest that = (CreatePaymentRequest) o;
        return Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency) && Objects.equals(intent, that.intent) && Objects.equals(description, that.description) && Objects.equals(cancelUrls, that.cancelUrls) && Objects.equals(successUrls, that.successUrls) && Objects.equals(scale, that.scale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency, intent, description, cancelUrls, successUrls, scale);
    }

    @Override
    public String toString() {
        return "CreatePaymentRequest{" +
                "amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", intent='" + intent + '\'' +
                ", description='" + description + '\'' +
                ", cancelUrls='" + cancelUrls + '\'' +
                ", successUrls='" + successUrls + '\'' +
                ", scale=" + scale +
                '}';
    }
}
