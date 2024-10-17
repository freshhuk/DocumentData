package com.document.documentdata;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentDataApplication {

    private final RabbitAdmin rabbitAdmin;

    private final Queue queue;

    @Autowired
    public DocumentDataApplication(RabbitAdmin rabbitAdmin, Queue queue) {
        this.rabbitAdmin = rabbitAdmin;
        this.queue = queue;
    }

    @PostConstruct
    public void declareQueue(){
        rabbitAdmin.declareQueue(queue);
    }

    public static void main(String[] args) {
        SpringApplication.run(DocumentDataApplication.class, args);
    }

}
