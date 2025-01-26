package com.lookingdev.stackoverflow.Controllers;

import com.lookingdev.stackoverflow.Services.ProfileProcessing;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEndPoints {

    private final ProfileProcessing service;

    public TestEndPoints(ProfileProcessing service){
        this.service = service;
    }

    @GetMapping("/init")
    public String init(){
        try{
            service.initDatabase();
            return "Ok";
        } catch (Exception ex){
            System.out.println("ERROR ERROR I HAVE A PROBLEM " + ex);
            return "Bad";
        }
    }
}
