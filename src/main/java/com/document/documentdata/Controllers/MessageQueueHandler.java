package com.document.documentdata.Controllers;

import com.document.documentdata.Domain.Enums.MessageAction;
import com.document.documentdata.Domain.Enums.QueueStatus;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Domain.Models.MessageWrapper;
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
    public MessageQueueHandler(DataService dataService, RabbitTemplate rabbitTemplate) {
        this.dataService = dataService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Method receive dto document model
     *
     * @param documentDTO - document model
     */
    @RabbitListener(queues = "dataQueue")
    public void receiveDocument(MessageWrapper documentDTO) {
        try {

            DocumentDTO document = (DocumentDTO) documentDTO.getPayload();
            MessageWrapper message = new MessageWrapper(MessageAction.UPLOAD.toString(), MessageAction.UPLOAD.toString());

            String result = dataService.add(document);
            if (result.equals(STATUS_CODE_200)) {
                logger.info("Success " + document.getFileName());
                sendMessage("StatusDataQueue", message);
                sendMessage("MongoQueue", documentDTO);

            } else {
                logger.error("DataService result exits with error " + documentDTO);
                MessageWrapper messageError = new MessageWrapper(MessageAction.UPLOAD.toString(), QueueStatus.BAD.toString());
                sendMessage("StatusDataQueue", messageError);
            }

        } catch (Exception ex) {
            logger.error("Error from receiveDocument " + ex);
            MessageWrapper messageError = new MessageWrapper(MessageAction.UPLOAD.toString(), QueueStatus.BAD.toString());
            sendMessage("StatusDataQueue", messageError);
        }

    }


    //Todo - удаление всех доков

    /*
    @RabbitListener(queues = "StatusDataQueue")
    public void receiveDataStatus(MessageWrapper message){
        try{
            if(message.getAction().equals(MessageAction.UPLOAD.toString())){

            }

            if(status.equals("Delete")){
                String result = dataService.deleteAllDocuments();
                if(result.equals("DeleteDone")){
                    sendMessage("StatusDataQueue", "DeleteMongo");
                    logger.info("Send status delete in mongo status queue");
                } else{
                    logger.error("Error with deleting entity ");
                }
            }
        } catch (Exception ex){
            logger.error("Error with receiveDataStatus");
        }
    }
*/


    @RabbitListener(queues = "StatusMongoQueue") //todo добавить удаление сущности
    public void receiveMongoStatus(MessageWrapper message) {

        if (message.getAction().equals(MessageAction.UPLOAD.toString())) {


            //&& message.getPayload() instanceof String может  этим придумать как то проверку на тип данных

            String receivedStatus = (String) message.getPayload();
            MessageWrapper sentMessage = new MessageWrapper();

            if (receivedStatus.equals(QueueStatus.DONE.toString())) {

                sentMessage.setAction(MessageAction.UPLOAD.toString());
                sentMessage.setPayload(QueueStatus.ALL_DONE.toString());

                logger.info("Result AllDone ");

                sendMessage("FinalStatusQueue", sentMessage);

            } else if (receivedStatus.equals(QueueStatus.BAD.toString())){

                String result = dataService.deleteDocument(receivedStatus);
                logger.info("Result of dataService deleteDocument " + result);

                sentMessage.setAction(MessageAction.UPLOAD.toString());
                sentMessage.setPayload(QueueStatus.ALL_ERROR.toString());

                sendMessage("FinalStatusQueue", sentMessage);

                logger.info(receivedStatus);
            }
        }
    }

    /**
     * Method sends message on rabbit queue
     * @param nameQueue - receiving queue
     * @param object - wrapper message
     */
    private void sendMessage(String nameQueue, MessageWrapper object) {
        rabbitTemplate.convertAndSend(nameQueue, object);
    }

}