package com.document.documentdata.Controllers;

import com.document.documentdata.Domain.Enums.QueueStatus;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Services.DataService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class MessageQueueHandler {

    private final DataService dataService;
    private final static String STATUS_CODE_200 = "200";
    /* Constant for queue */
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MessageQueueHandler.class);



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
                logger.info("Success " + documentDTO.getFileName());
                sendMessage("StatusDataQueue", QueueStatus.DONE.toString());
                sendMessage("MongoQueue", documentDTO);

            }else{
                logger.error("DataService result exits with error " + documentDTO.getFileName());
                sendMessage("StatusDataQueue", QueueStatus.BAD.toString());
            }

        }catch (Exception ex){
            logger.error("Error from receiveDocument " + ex);
            sendMessage("StatusDataQueue", QueueStatus.BAD.toString());
        }

    }
    @RabbitListener(queues = "StatusMongoQueue")
    public void receiveMongoStatus(String status){
        boolean statusL = status.length() == 3;

        if(status.equals(QueueStatus.DONE.toString())){
            sendMessage("StatusDataQueue", "AllDone");
        } else if (!statusL) {
            String result = dataService.deleteDocument(status);
            logger.info("Result of dataService deleteDocument " + result);
            sendMessage("StatusDataQueue", "AllError");
        }
        else{
            sendMessage("StatusDataQueue", "AllError");
            logger.info(status);
        }
    }

    private void sendMessage(String nameQueue, String status){
        rabbitTemplate.convertAndSend(nameQueue, status);
    }
    private void sendMessage(String nameQueue, DocumentDTO docDTO){
        rabbitTemplate.convertAndSend(nameQueue, docDTO);
    }

}
