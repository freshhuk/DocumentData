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
    public void receiveDocument(MessageWrapper<DocumentDTO> documentDTO) {
        try {
            System.out.println(documentDTO.getAction() + " " + documentDTO.getPayload().toString());


            DocumentDTO document = documentDTO.getPayload();
            MessageWrapper<String> message = new MessageWrapper<>(MessageAction.UPLOAD.toString(), MessageAction.UPLOAD.toString());


            String result = dataService.add(document);
            if (result.equals(STATUS_CODE_200)) {
                logger.info("Success " + document.getFileName());
                sendMessage("StatusDataQueue", message);
                sendMessage("MongoQueue", documentDTO);

            } else {
                logger.error("DataService result exits with error " + documentDTO);
                MessageWrapper<String> messageError = new MessageWrapper<>(MessageAction.UPLOAD.toString(), QueueStatus.BAD.toString());
                sendMessage("StatusDataQueue", messageError);
            }

        } catch (Exception ex) {
            logger.error("Error from receiveDocument " + ex);
            MessageWrapper<String> messageError = new MessageWrapper<>(MessageAction.UPLOAD.toString(), QueueStatus.BAD.toString());
            sendMessage("StatusDataQueue", messageError);
        }

    }


    //Todo - удаление всех доков + всю логику спрятать в сервисы и провести декомпозицию, что б слушатели были мелкими

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


    @RabbitListener(queues = "StatusMongoQueue")
    public void receiveMongoStatus(MessageWrapper<MessageWrapper<DocumentDTO>> message) {

        if (message.getAction().equals(MessageAction.UPLOAD.toString())) {


            //Message with model and status
            MessageWrapper<DocumentDTO> receivedMessage = message.getPayload();

            MessageWrapper<String> sentMessage = new MessageWrapper<>();

            String modelStatus = receivedMessage.getAction();
            DocumentDTO receivedModel = receivedMessage.getPayload();

            if (modelStatus.equals(QueueStatus.DONE.toString())) {

                sentMessage.setAction(MessageAction.UPLOAD.toString());
                sentMessage.setPayload(QueueStatus.ALL_DONE.toString());

                logger.info("Result AllDone. Received model: " + receivedModel.toString());

                sendMessage("FinalStatusQueue", sentMessage);

            } else if (modelStatus.equals(QueueStatus.BAD.toString()) && receivedModel != null){

                String result = dataService.deleteDocument(receivedModel.getFileName());
                logger.info("Result of dataService deleteDocument " + result);

                sentMessage.setAction(MessageAction.UPLOAD.toString());
                sentMessage.setPayload(QueueStatus.ALL_ERROR.toString());

                sendMessage("FinalStatusQueue", sentMessage);

                logger.info("Document " + receivedModel.getFileName() + " was deleted");
            }
        }
    }

    /**
     * Method sends message on rabbit queue
     * @param nameQueue - receiving queue
     * @param object - wrapper message
     */
    private void sendMessage(String nameQueue, MessageWrapper<?> object) {
        rabbitTemplate.convertAndSend(nameQueue, object);
    }

}