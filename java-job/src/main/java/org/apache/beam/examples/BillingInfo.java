package org.apache.beam.examples;

import org.apache.avro.reflect.Nullable;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class BillingInfo implements Serializable {
    @Nullable
    @JsonProperty("payment_method")
    private String payment_method;
    @Nullable
    @JsonProperty("card_number")
    private String card_number;
    @Nullable
    @JsonProperty("currency")
    private String currency;

    @Override
    public String toString() {
        return "BillingInfo{" +
                "payment_method='" + payment_method + '\'' +
                ", card_number='" + card_number + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
