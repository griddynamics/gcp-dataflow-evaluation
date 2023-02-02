package org.apache.beam.examples;

import org.codehaus.jackson.annotate.JsonProperty;
import org.apache.avro.reflect.Nullable;
import java.io.Serializable;

public class Order implements Serializable {
    @JsonProperty("user_id")
    private long user_id;
    @JsonProperty("order_id")
    private String order_id;
    @Nullable
    @JsonProperty("device_name")
    private String device_name;
    @Nullable
    @JsonProperty("price")
    private long price;

    @Override
    public String toString() {
        return "Order{" +
                "user_id=" + user_id +
                ", order_id='" + order_id + '\'' +
                ", device_name='" + device_name + '\'' +
                ", price=" + price +
                ", amount=" + amount +
                ", order_date='" + order_date + '\'' +
                ", order_type='" + order_type + '\'' +
                ", shipping_info=" + shipping_info +
                ", billing_info=" + billing_info +
                '}';
    }
    @Nullable
    @JsonProperty("amount")
    private long amount;
    @Nullable
    @JsonProperty("order_date")
    private String order_date;
    @Nullable
    @JsonProperty("order_type")
    private String order_type;
    @Nullable
    @JsonProperty("shipping_info")
    private ShippingInfo shipping_info;
    @Nullable
    @JsonProperty("billing_info")
    private BillingInfo billing_info;

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public String getOrder_type() {
        return order_type;
    }

    public void setOrder_type(String order_type) {
        this.order_type = order_type;
    }

    public ShippingInfo getShipping_info() {
        return shipping_info;
    }

    public void setShipping_info(ShippingInfo shipping_info) {
        this.shipping_info = shipping_info;
    }

    public BillingInfo getBilling_info() {
        return billing_info;
    }

    public void setBilling_info(BillingInfo billing_info) {
        this.billing_info = billing_info;
    }


}
