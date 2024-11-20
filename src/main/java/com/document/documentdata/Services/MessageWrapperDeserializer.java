package com.document.documentdata.Services;

import com.document.documentdata.Domain.Models.DocumentDTO;
import com.document.documentdata.Domain.Models.MessageModel;
import com.document.documentdata.Domain.Models.MessageWrapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class MessageWrapperDeserializer extends JsonDeserializer<MessageWrapper<?>> {

    @Override
    public MessageWrapper<?> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        String action = node.get("action").asText();

        Object payload;

        if (node.has("payload")) {
            JsonNode payloadNode = node.get("payload");

            // Logic for strings
            if (payloadNode.isTextual()) {
                payload = payloadNode.asText();  // String
            }
            //Log for another classes
            else if (payloadNode.has("fileName") && payloadNode.has("fileType")) {//  DocumentDTO
                payload = jp.getCodec().treeToValue(payloadNode, DocumentDTO.class);
            }
            else if (payloadNode.has("status") && payloadNode.has("documentModel")) {
                payload = jp.getCodec().treeToValue(payloadNode, MessageModel.class);  // MessageModel
            }else {
                payload = jp.getCodec().treeToValue(payloadNode, Object.class); //Object
            }
        } else {
            payload = null;
        }

        return new MessageWrapper<>(action, payload);
    }
}

