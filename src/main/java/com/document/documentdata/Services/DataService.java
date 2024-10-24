package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DataService {

    private final DocumentRepository repository;
    private final static String STATUS_CODE_200 = "200";

    @Autowired
    public DataService(DocumentRepository repository) {
        this.repository = repository;
    }


    /**
     * Method save document in db
     * @param doc - document model
     */
    public String add(DocumentDTO doc) {
        try {
            if (!repository.docIsExist(doc.getFileName())) {

                LocalDate currentDate = LocalDate.now();

                Document document = new Document();
                {
                    document.setName(doc.getFileName());
                    document.setDocType(doc.getFileType());
                    document.setCreatedDate(currentDate);
                    document.setModifyDate(currentDate);
                }
                document.setIdUserCreate(1);//todo
                document.setIdUserModify(1);//todo

                repository.add(document);
            } else {
                System.out.println("Update");
                updateDocument(doc);
            }
            return STATUS_CODE_200;
        } catch (Exception ex) {
            System.out.println("Error in add method " + ex);
            return "ERROR";
        }
    }

    private void updateDocument(DocumentDTO doc) {
        try {
            Document document = repository.getByName(doc.getFileName());
            LocalDate currentDate = LocalDate.now();
            document.setModifyDate(currentDate);
            document.setIdUserModify(1);//todo

            repository.update(document);
        } catch (Exception ex) {
            System.out.println("Error in updateDocument method " + ex);
        }
    }

    /**
     * Method for deleting document from postgres database
     * @param status - document status
     * @return - method status
     */
    public String deleteDocument(String status){
        try{
            String fileName = parseStatus(status);
            var model = repository.getByName(fileName);
            if(model!= null){
                repository.deleteById(model.getId());
                return "DeleteDone";
            } else {
                System.out.println("deleteDocument: Model is null");
                return "ErrorDelete";
            }
        } catch (Exception ex){
            return "ErrorDelete";
        }
    }

    private String parseStatus(String status){
        StringBuilder stringBuilder = new StringBuilder(status);
        stringBuilder.delete(0, 3);
        status = stringBuilder.toString();
        return  status;

    }
}
