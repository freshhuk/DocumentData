package com.document.documentdata.Services;

import com.document.documentdata.Domain.Enums.MessageAction;
import com.document.documentdata.Domain.Enums.QueueStatus;
import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Domain.Models.MessageModel;
import com.document.documentdata.Domain.Models.MessageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    /* Constant for queue */
    private final static String STATUS_CODE_200 = "200";
    private final DataService dataService;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    public MessageService(DataService dataService, RabbitTemplate rabbitTemplate) {
        this.dataService = dataService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDocumentInQueue(MessageWrapper<DocumentDTO> documentDTO){
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

    /**
     * Method for processing message from mongo queue: action - upload
     * @param message message from mongo queue
     */
    public void sendFinalStatusUpload(MessageWrapper<MessageModel> message){
        try{
            //Message with model and status
            MessageModel receiveModel = message.getPayload();

            MessageWrapper<String> sentMessage = new MessageWrapper<>();

            String modelStatus = receiveModel.getStatus();
            DocumentDTO receivedModel = receiveModel.getDocumentModel();

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
        }catch (Exception ex){
            logger.error("Error with final results: " + ex);
        }

    }

    /**
     * Method delete entity in postgres db and send delete message on mongo db
     * @param message sent message
     */
    public void sendDeleteStatus(MessageWrapper<String> message){
        try{
            String result = dataService.deleteAllDocuments();
            if(result.equals(QueueStatus.DONE.toString())){
                sendMessage("StatusDataQueue", message);
                logger.info("Delete was successful");
            } else{
                MessageWrapper<String> errorMessage = new MessageWrapper<>(MessageAction.DELETE.toString(), QueueStatus.BAD.toString());
                sendMessage("FinalStatusQueue", errorMessage);
                logger.warn("Problems with deleting entities");
            }
        } catch (Exception ex){
            logger.error("Error with delete entity " + ex);
        }
    }
    /**
     * Method for processing message from mongo queue: action - delete
     * @param message message from mongo queue
     */
    public void sendFinalStatusDelete(MessageWrapper<String> message){
        try{
            MessageWrapper<String> finalMessage;
            if(message.getPayload().equals(QueueStatus.DONE.toString())){
                finalMessage = new MessageWrapper<>(MessageAction.DELETE.toString(), QueueStatus.ALL_DONE.toString());
                logger.info("Delete was successful");
                sendMessage("FinalStatusQueue", finalMessage);
            } else{
                finalMessage = new MessageWrapper<>(MessageAction.DELETE.toString(), QueueStatus.ALL_ERROR.toString());
                logger.warn("Delete with some problems");
                sendMessage("FinalStatusQueue", finalMessage);
            }
        } catch (Exception ex){
            logger.error("Error with delete " + ex);
            MessageWrapper<String>  finalMessage = new MessageWrapper<>(MessageAction.DELETE.toString(), QueueStatus.ALL_ERROR.toString());
            sendMessage("FinalStatusQueue", finalMessage);
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
