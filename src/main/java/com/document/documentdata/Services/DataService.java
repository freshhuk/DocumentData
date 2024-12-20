package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Enums.QueueStatus;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DataService {

    private final DocumentRepository repository;
    private final static String STATUS_CODE_200 = "200";
    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

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
                logger.info("Entity was added in db");
            } else {
                logger.info("Entity was updated in db");
                updateDocument(doc);
            }
            return STATUS_CODE_200;
        } catch (Exception ex) {
            logger.error("Error with adding entity in  db: " + ex);
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
            logger.info("Successful update");
        } catch (Exception ex) {
            logger.error("Error in updateDocument method: " + ex);
        }
    }

    /**
     * Method for deleting document from postgres database
     * @param fileName document name
     * @return - method status
     */
    public String deleteDocument(String fileName){
        try{
            var model = repository.getByName(fileName);
            if(model != null){
                repository.deleteById(model.getId());
                logger.info("Document was deleted");
                return QueueStatus.DONE.toString();
            } else {
                logger.error("deleteDocument: Model is null");
                return QueueStatus.BAD.toString();
            }
        } catch (Exception ex){
            logger.error("Error with deleteDocument method: " + ex);
            return QueueStatus.BAD.toString();
        }
    }

    /**
     * Method deletes all entity from postgres database
     * @return method - status
     */
    public String deleteAllDocuments(){
        try{
            repository.deleteAll();
            logger.info("Documents were deleted");
            return QueueStatus.DONE.toString();

        } catch (Exception ex){

            logger.error("Error with deleteAllDocument method: " + ex);
            return QueueStatus.BAD.toString();
        }
    }
}
