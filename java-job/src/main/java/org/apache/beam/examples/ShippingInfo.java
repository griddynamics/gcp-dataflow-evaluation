package org.apache.beam.examples;

import org.apache.avro.reflect.Nullable;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class ShippingInfo implements Serializable {
    @Nullable
    @JsonProperty("shipping_address")
    private String shipping_address;
    @Nullable
    @JsonProperty("postal_code")
    private String postal_code;

    public String getShipping_address() {
        return shipping_address;
    }

    @Override
    public String toString() {
        return "ShippingInfo{" +
                "shipping_address='" + shipping_address + '\'' +
                ", postal_code='" + postal_code + '\'' +
                '}';
    }

    public void setShipping_address(String shipping_address) {
        this.shipping_address = shipping_address;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }
}