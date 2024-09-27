package com.document.documentdata.Models.Entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "docType")
    private String docType;

    @Column(name = "createdDate")
    private String createdDate;

    @Column(name = "modifyDate")
    private String modifyDate;

    @Column(name = "idUserCreate")
    private int idUserCreate;

    @Column(name = "idUserModify")
    private int idUserModify;
}
