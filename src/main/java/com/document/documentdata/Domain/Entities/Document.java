package com.document.documentdata.Domain.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "documents")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "docType")
    private String docType;

    @Column(name = "createdDate")
    private LocalDate createdDate;

    @Column(name = "modifyDate")
    private LocalDate modifyDate;

    @Column(name = "idUserCreate")
    private int idUserCreate;

    @Column(name = "idUserModify")
    private int idUserModify;
}
