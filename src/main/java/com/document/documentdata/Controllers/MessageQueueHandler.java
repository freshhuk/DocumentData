package com.document.documentdata.Controllers;

import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Services.DataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageQueueHandler {

    private final DataService dataService;
    private final static String STATUS_CODE_200 = "200";
    /* Constant for queue */
    private final static String STATUS_ERROR_API = "ErrorFromData";
    private final RabbitTemplate rabbitTemplate;



    @Autowired
    public MessageQueueHandler(DataService dataService, RabbitTemplate rabbitTemplate){
        this.dataService = dataService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Method receive dto document model
     *
     * @param documentDTO - document model
     */
    @RabbitListener(queues = "dataQueue")
    public void receiveDocument(DocumentDTO documentDTO) {
        try{
            String result = dataService.add(documentDTO);
            if(result.equals(STATUS_CODE_200)){
                System.out.println("Success " + documentDTO.getFileName());
                sendMessage("StatusDataQueue", "Done");
                sendMessage("MongoQueue", documentDTO);

            }else{
                System.out.println("Ops.. " + documentDTO.getFileName());
                sendMessage("StatusDataQueue", STATUS_ERROR_API);
            }

        }catch (Exception ex){
            System.out.println("Error from receiveDocument : " + ex);
            sendMessage("StatusDataQueue", STATUS_ERROR_API);
        }

    }
    @RabbitListener(queues = "StatusMongoQueue")
    public void receiveMongoStatus(String status){
        boolean statusL = status.length() == 3;

        if(status.equals("Done")){
            sendMessage("StatusDataQueue", "AllDone");
        } else if (statusL) {
            String result = dataService.deleteDocument(status);
            System.out.println(result);
            sendMessage("StatusDataQueue", "AllError");
        }
    }

    private void sendMessage(String nameQueue, String status){
        rabbitTemplate.convertAndSend(nameQueue, status);
    }
    private void sendMessage(String nameQueue, DocumentDTO docDTO){
        rabbitTemplate.convertAndSend(nameQueue, docDTO);
    }

}
