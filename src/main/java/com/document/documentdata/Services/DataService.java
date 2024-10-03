package com.document.documentdata.Services;

import com.document.documentdata.Domain.Entities.Document;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Repositories.DocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        System.out.println("Success "+documentDTO.getFileName());
    }


    /**
     * Method save document in db
     * @param doc - document model
     */
    public String add(Document doc){
        try{
            if(isValid(doc)){
                repository.add(doc);
                return STATUS_CODE_200;
            } else{
                return "is not valid";
            }
        } catch (Exception ex){
            System.out.println("Error in add method " + ex);
            return "ERROR";
        }

    }
    private boolean isValid(Document doc){
        if (repository.docIsExist(doc.getName())){
            String lastFourCharacters = doc.getName().substring(doc.getName().length() - 5);
            return lastFourCharacters.equals(".docx");
        }
        return false;
    }
}
