package com.lookingdev.stackoverflow.Services;

import com.lookingdev.stackoverflow.Domain.Enums.QueueAction;
import com.lookingdev.stackoverflow.Domain.Models.MessageModel;
import com.lookingdev.stackoverflow.Domain.Models.MessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Value("${queueStackOverflowStatus.name}")
    private String queueStackName;

    private final ProfileProcessing profileService;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);


    @Autowired
    public MessageService(ProfileProcessing profileService, RabbitTemplate rabbitTemplate) {
        this.profileService = profileService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void initDB() {
        try{
            profileService.initDatabase();
            LOGGER.info("init was successful");
        } catch (Exception ex){
            LOGGER.error("Error with adding user in db");
        }
    }

    public void getStackOverflowUsers(MessageStatus message) {

        try {
            if (message.getAction().equals(QueueAction.GET_STACK_USER)) {
                //Parsing message for get last entity index
                int lastIndex = (Integer.parseInt(message.getStatus())) * 10;

                MessageModel messageWithData = new MessageModel();
                messageWithData.setAction(QueueAction.GET_STACK_USER);
                messageWithData.setDeveloperProfiles(profileService.getDevelopersDTO(lastIndex));

                sendDataInQueue(queueStackName, messageWithData);
                LOGGER.info("User was sent in queue");
            }
        } catch (Exception ex) {
            LOGGER.error("Error with get gitHub users {}", String.valueOf(ex));
        }
    }

    /**
     * Method for sending status message in queue
     *
     * @param queueName queue name
     * @param message   message which be sent in the queue
     */
    private void sendDataInQueue(String queueName, MessageModel message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}
