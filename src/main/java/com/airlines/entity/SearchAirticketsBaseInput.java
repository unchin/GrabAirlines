package com.airlines.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class SearchAirticketsBaseInput {
    @JsonProperty("NeedOrginHtml")
    private Boolean needOrginHtml;

    @JsonProperty("Authrazation")
    private SearchAirticketsInputAuthorization authrazation;

    @JsonProperty("Data")
    private SearchAirticketsInput data;
}
