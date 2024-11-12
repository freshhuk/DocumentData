package com.document.documentdata.Domain.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageWrapper {
    @JsonProperty("action")
    private String action; // type: "add", "delete", "update" and so on
    @JsonProperty("payload")
    private Object payload;
}
