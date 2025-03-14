package com.lookingdev.stackoverflow.Controllers;

import com.lookingdev.stackoverflow.Domain.Enums.QueueAction;
import com.lookingdev.stackoverflow.Domain.Models.MessageStatus;
import com.lookingdev.stackoverflow.Services.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageQueueHandler {

    private final MessageService messageService;

    public MessageQueueHandler(MessageService messageService){
        this.messageService = messageService;
    }
    /**
     * Receive API status for get Stack Overflow users
     */
    @RabbitListener(queues = "APIStatusQueue")
    public void listenAPIStatus(MessageStatus message){
        if(message.getAction().equals(QueueAction.GET_STACK_USER)){
            messageService.getStackOverflowUsers(message);
        } else if (message.getAction().equals(QueueAction.INIT_DB_STACK_OVERFLOW)) {
            messageService.initDB();
        } else if (message.getAction().equals(QueueAction.GET_INIT_STATUS_STACK_OVERFLOW)) {
            messageService.getInitStatus();
        }
    }


    @RabbitListener(queues = "GitSagaChainQueue")
    public void listenGitStatus(MessageStatus message){
        if(message.getAction().equals(QueueAction.GET_STACK_USER)){
            messageService.getAllUsers(message);
        }
    }
}
