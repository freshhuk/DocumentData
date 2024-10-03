package com.document.documentdata.Domain.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {

    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("fileType")
    private String fileType;
}
