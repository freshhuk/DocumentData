package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class DataService {


    //TODO: кароче надо создать вторую очередь для обработки ошибок, и к примеру если что то случилось неудачное
    //то отправлять ошибку в канал с ошибками, а в других микросервисах
    // это читать и если мы поучаем ошибку то тогда отменять все наши действия
    //Или не то что отменять просто использовать другую логику
    private final DocumentRepository repository;
    private final static String STATUS_CODE_200 = "200";

    @Autowired
    public DataService(DocumentRepository repository){
        this.repository = repository;
    }

    //Test
    @RabbitListener(queues = "FirstQueue")
    public void receiveDocument(DocumentDTO documentDTO){
        add(documentDTO);
        System.out.println("Success "+documentDTO.getFileName());
    }


    /**
     * Method save document in db
     * @param doc - document model
     */
    public String add(DocumentDTO doc){
        try{
            if(isExist(doc)){

                LocalDate currentDate = LocalDate.now();

                Document document = new Document();{
                    document.setName(doc.getFileName());
                    document.setDocType(doc.getFileType());
                    document.setCreatedDate(currentDate);
                    document.setModifyDate(currentDate);
                }
                document.setIdUserCreate(1);//todo
                document.setIdUserModify(1);//todo

                repository.add(document);
            } else{
                updateDocument(doc);
            }
            return STATUS_CODE_200;
        } catch (Exception ex){
            System.out.println("Error in add method " + ex);
            return "ERROR";
        }
    }
    private void updateDocument(DocumentDTO doc){
        try{
            Document document = repository.getByName(doc.getFileName());
            LocalDate currentDate = LocalDate.now();
            document.setModifyDate(currentDate);
            document.setIdUserModify(1);//todo

            repository.update(document);
        } catch (Exception ex){
            System.out.println("Error in updateDocument method " + ex);
        }
    }
    private boolean isExist(DocumentDTO doc){
        if (repository.docIsExist(doc.getFileName())){
            String lastFourCharacters = doc.getFileName().substring(doc.getFileName().length() - 5);
            return lastFourCharacters.equals(".docx");
        }
        return false;
    }
}
