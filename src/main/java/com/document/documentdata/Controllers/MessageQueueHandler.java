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
    @RabbitListener(queues = "FirstQueue")
    public void receiveDocument(DocumentDTO documentDTO) {
        try{
            String result = dataService.add(documentDTO);
            if(result.equals(STATUS_CODE_200)){
                System.out.println("Success " + documentDTO.getFileName());
                //TODO в случае успеха кинуть смс на монго сервак
            }else{
                System.out.println("Ops.. " + documentDTO.getFileName());
                //кинуть смс о проблеме на апишку
            }

        }catch (Exception ex){
            System.out.println("Error from receiveDocument : " + ex);
            //кинуть смс о проблеме на апишку
        }

    }
}
