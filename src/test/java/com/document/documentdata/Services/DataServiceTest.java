package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class DataServiceTest {

    private final static String STATUS_CODE_200 = "200";

    @InjectMocks
    private DataService service;
    @Mock
    private DocumentRepository repository;

    @Test
    void addTestValid(){

        DocumentDTO testModel = new DocumentDTO();
        testModel.setFileName("Test.docx");
        testModel.setFileType(".docx");

        Mockito.when(repository.docIsExist(testModel.getFileName())).thenReturn(false);

        var result = service.add(testModel);

        Assertions.assertEquals(result, STATUS_CODE_200);

    }
    @Test
    void addTestUpdate(){

        Document testDocument = new Document();{
            testDocument.setId(1);
            testDocument.setName("Test.docx");
            testDocument.setDocType(".docx");
            testDocument.setIdUserModify(1);
            testDocument.setIdUserCreate(1);
            testDocument.setCreatedDate(LocalDate.now());
        }

        DocumentDTO testModel = new DocumentDTO();
        testModel.setFileName("Test.docx");
        testModel.setFileType(".docx");

        Mockito.when(repository.docIsExist(testModel.getFileName())).thenReturn(true);
        Mockito.when(repository.getByName(testModel.getFileName())).thenReturn(testDocument);

        var result = service.add(testModel);

        Assertions.assertEquals(result, STATUS_CODE_200);

    }

    @Test
    void deleteDocumentTest(){

        Document testDocument = new Document();{
            testDocument.setId(1);
            testDocument.setName("test.docx");
            testDocument.setDocType(".docx");
            testDocument.setIdUserModify(1);
            testDocument.setIdUserCreate(1);
            testDocument.setCreatedDate(LocalDate.now());
        }

        Mockito.when(repository.getByName("test")).thenReturn(testDocument);

        var result = service.deleteDocument("BADtest");

        Assertions.assertEquals(result, "DeleteDone");
    }
    @Test
    void deleteDocumentIsNullTest(){

        Mockito.when(repository.getByName("test")).thenReturn(null);

        var result = service.deleteDocument("BADtest");

        Assertions.assertEquals(result, "ErrorDelete9");
    }
}
