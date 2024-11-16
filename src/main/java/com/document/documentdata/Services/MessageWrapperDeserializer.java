package com.document.documentdata.Services;

import com.document.documentdata.Domain.Models.DocumentDTO;
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

            // Логика для обработки строки
            if (payloadNode.isTextual()) {
                payload = payloadNode.asText();  // Если это строка
            }
            // Логика для десериализации объекта DocumentDTO
            else if (payloadNode.has("fileName") && payloadNode.has("fileType")) {
                payload = jp.getCodec().treeToValue(payloadNode, DocumentDTO.class);  // Если это объект DocumentDTO
            } else {
                payload = jp.getCodec().treeToValue(payloadNode, Object.class); // Дефолтная обработка
            }
        } else {
            payload = null;
        }

        return new MessageWrapper<>(action, payload);
    }
}

