package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Enums.QueueStatus;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataServiceTest {

    @Mock
    private DocumentRepository repository;

    @InjectMocks
    private DataService dataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdd_NewDocument_Success() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO("TestFile", "txt");
        when(repository.docIsExist(documentDTO.getFileName())).thenReturn(false);

        // Act
        String result = dataService.add(documentDTO);

        // Assert
        assertEquals("200", result);
        verify(repository, times(1)).add(any(Document.class));
        verify(repository, never()).update(any(Document.class));
    }

    @Test
    void testAdd_ExistingDocument_Update() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO("TestFile", "txt");
        when(repository.docIsExist(documentDTO.getFileName())).thenReturn(true);
        when(repository.getByName(documentDTO.getFileName())).thenReturn(new Document());

        // Act
        String result = dataService.add(documentDTO);

        // Assert
        assertEquals("200", result);
        verify(repository, never()).add(any(Document.class));
        verify(repository, times(1)).update(any(Document.class));
    }

    @Test
    void testAdd_ExceptionHandling() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO("TestFile", "txt");
        when(repository.docIsExist(any())).thenThrow(new RuntimeException("Database error"));

        // Act
        String result = dataService.add(documentDTO);

        // Assert
        assertEquals("ERROR", result);
    }

    @Test
    void testDeleteDocument_Success() {
        // Arrange
        Document document = new Document();
        document.setId(1);
        document.setName("TestFile");
        when(repository.getByName("TestFile")).thenReturn(document);

        // Act
        String result = dataService.deleteDocument("TestFile");

        // Assert
        assertEquals(QueueStatus.DONE.toString(), result);
        verify(repository, times(1)).deleteById(document.getId());
    }

    @Test
    void testDeleteDocument_NotFound() {
        // Arrange
        when(repository.getByName("NonExistentFile")).thenReturn(null);

        // Act
        String result = dataService.deleteDocument("NonExistentFile");

        // Assert
        assertEquals(QueueStatus.BAD.toString(), result);
        verify(repository, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteDocument_ExceptionHandling() {
        // Arrange
        when(repository.getByName(any())).thenThrow(new RuntimeException("Database error"));

        // Act
        String result = dataService.deleteDocument("TestFile");

        // Assert
        assertEquals(QueueStatus.BAD.toString(), result);
    }

    @Test
    void testDeleteAllDocuments_Success() {
        // Act
        String result = dataService.deleteAllDocuments();

        // Assert
        assertEquals(QueueStatus.DONE.toString(), result);
        verify(repository, times(1)).deleteAll();
    }

    @Test
    void testDeleteAllDocuments_ExceptionHandling() {
        // Arrange
        doThrow(new RuntimeException("Database error")).when(repository).deleteAll();

        // Act
        String result = dataService.deleteAllDocuments();

        // Assert
        assertEquals(QueueStatus.BAD.toString(), result);
    }
}