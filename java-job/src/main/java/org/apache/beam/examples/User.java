package org.apache.beam.examples;

import org.apache.avro.reflect.Nullable;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class User implements Serializable {
    @JsonProperty("user_id")
    private long user_id;
    @JsonProperty("username")
    private String username;
    @Nullable
    @JsonProperty("birth_date")
    private String birth_date;
    @Nullable
    @JsonProperty("email")
    private String email;
    @Nullable
    @JsonProperty("country")
    private String country;

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", username='" + username + '\'' +
                ", birth_date='" + birth_date + '\'' +
                ", email='" + email + '\'' +
                ", country='" + country + '\'' +
                ", code='" + code + '\'' +
                ", account_creation_date='" + account_creation_date + '\'' +
                '}';
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccount_creation_date() {
        return account_creation_date;
    }

    public void setAccount_creation_date(String account_creation_date) {
        this.account_creation_date = account_creation_date;
    }
    @Nullable
    @JsonProperty("code")
    private String code;
    @Nullable
    @JsonProperty("account_creation_date")
    private String account_creation_date;
}
