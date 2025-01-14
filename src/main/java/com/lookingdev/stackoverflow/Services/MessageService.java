package com.lookingdev.stackoverflow.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final ProfileProcessing profileService;
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    public MessageService(ProfileProcessing profileService) {
        this.profileService = profileService;
    }

    public void initDB() {
        try{
            profileService.initDatabase();
            LOGGER.info("init was successful");
        } catch (Exception ex){
            LOGGER.error("Error with adding user in db");
        }
    }

    public void getStackOverflowUsers() {
    }
}
