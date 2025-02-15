package com.lookingdev.stackoverflow.Services;

import com.lookingdev.stackoverflow.Domain.Entities.DeveloperProfile;
import com.lookingdev.stackoverflow.Domain.Models.DeveloperDTOModel;
import com.lookingdev.stackoverflow.Repository.DevelopersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileProcessing {

    private final StackOverflowService overflowService;
    private final DevelopersRepository repository;

    /* CONSTANTS */
    private static final int LIMIT_USERS = 10;


    @Autowired
    public ProfileProcessing(StackOverflowService overflowService, DevelopersRepository repository){
        this.overflowService = overflowService;
        this.repository = repository;
    }


    public void initDatabase(){
        List<DeveloperProfile> developers = parseToDeveloperProfile(overflowService.fetchUsers());
        repository.saveAll(developers);
    }

    public List<DeveloperDTOModel> getDevelopersDTO(int lastIndex){

        Pageable pageable = PageRequest.of(lastIndex, LIMIT_USERS, Sort.by(Sort.Direction.ASC, "id"));
        List<DeveloperProfile> developers = repository.findAll(pageable).getContent();
        if (developers.isEmpty()) {
            return List.of(); // if data null or empty
        }
        return convertToDTO(developers);
    }

    /**
     * Convert list from DTO model in entity
     * @param listDTO list DTO models
     * @return converted list
     */
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

    /**
     * Convert list from entity model in DTO
     * @param developerProfiles list entity models
     * @return converted list
     */
    private List<DeveloperDTOModel> convertToDTO(List<DeveloperProfile> developerProfiles) {
        return developerProfiles.stream()
                .map(profile -> new DeveloperDTOModel(
                        profile.getPlatform(),
                        profile.getUsername(),
                        profile.getProfileUrl(),
                        profile.getReputation(),
                        List.of(profile.getSkills()), // Convert array in List
                        profile.getLocation(),
                        profile.getLastActivityDate()
                ))
                .collect(Collectors.toList());
    }
}
