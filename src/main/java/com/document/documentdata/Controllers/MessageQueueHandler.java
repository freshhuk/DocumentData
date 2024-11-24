package com.document.documentdata.Controllers;

import com.document.documentdata.Domain.Enums.MessageAction;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Domain.Models.MessageModel;
import com.document.documentdata.Domain.Models.MessageWrapper;
import com.document.documentdata.Services.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MessageQueueHandler {

    private final MessageService messageService;
    @Autowired
    public MessageQueueHandler( MessageService messageService) {
        this.messageService =  messageService;
    }

    /**
     * Method receive dto document model
     *
     * @param documentDTO - document model
     */
    @RabbitListener(queues = "dataQueue")
    public void receiveDocument(MessageWrapper<DocumentDTO> documentDTO) {
        if (documentDTO.getAction().equals(MessageAction.UPLOAD.toString())){
            messageService.sendDocumentInQueue(documentDTO);
        }
    }

    /**
     * Method deletes all entities and send status on mongo queue
     * @param message message
     */
    @RabbitListener(queues = "queueAPIStatus")
    public void receiveDataStatus(MessageWrapper<String> message){

        if(message.getAction().equals(MessageAction.DELETE.toString()) && message.getPayload().equals(MessageAction.DELETE.toString())){
            messageService.sendDeleteStatus(message);
        }
    }

    /**
     * Method gets final status with upload document
     * @param message message
     */
    @RabbitListener(queues = "StatusMongoQueue")
    public void receiveMongoStatus(MessageWrapper<?> message) {
        if (message.getAction().equals(MessageAction.UPLOAD.toString()) && message.getPayload() instanceof MessageModel) {
            messageService.sendFinalStatusUpload((MessageWrapper<MessageModel>)message);
            System.out.println("Well done");
        }
        else if (message.getAction().equals(MessageAction.DELETE.toString()) && message.getPayload() instanceof String) {
            messageService.sendFinalStatusDelete((MessageWrapper<String>)message);

        }
    }

}