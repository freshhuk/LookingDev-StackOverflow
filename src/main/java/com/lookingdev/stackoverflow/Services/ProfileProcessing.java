package com.lookingdev.stackoverflow.Services;

import com.lookingdev.stackoverflow.Domain.Entities.DeveloperProfile;
import com.lookingdev.stackoverflow.Domain.Models.DeveloperDTOModel;
import com.lookingdev.stackoverflow.Repository.DevelopersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileProcessing {

    private final StackOverflowService overflowService;
    private final DevelopersRepository repository;

    /* CONSTANTS */
    private static final int USER_COUNT_IN_DB = 100;

    @Autowired
    public ProfileProcessing(StackOverflowService overflowService, DevelopersRepository repository){
        this.overflowService = overflowService;
        this.repository = repository;
    }


    public void initDatabase(){
        List<DeveloperProfile> developers = parseToDeveloperProfile(overflowService.fetchUsers(USER_COUNT_IN_DB));
        repository.saveAll(developers);
    }
    private List<DeveloperProfile> parseToDeveloperProfile(List<DeveloperDTOModel> listDTO) {
        return listDTO.stream()
                .map(profile -> new DeveloperProfile(
                        null,
                        profile.getPlatform(),
                        profile.getUsername(),
                        profile.getProfileUrl(),
                        profile.getReputation(),
                        profile.getSkills().toArray(new String[0]),
                        profile.getLocation(),
                        profile.getLastActivityDate()
                ))
                .collect(Collectors.toList());
    }


}
