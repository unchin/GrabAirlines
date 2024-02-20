package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchAirticketsInputAuthorization {
    @JsonProperty("AppId")
    private String appId;

    @JsonProperty("TimeSpan")
    private String timeSpan;

    @JsonProperty("Token")
    private String token;
}
