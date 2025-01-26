package com.lookingdev.stackoverflow.Repository;

import com.lookingdev.stackoverflow.Domain.Entities.DeveloperProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DevelopersRepository extends MongoRepository<DeveloperProfile, String> {

    @Query("{ 'id': { $gt: ?0 } }")
    List<DeveloperProfile> findDevelopersWithLimit(String lastId, Pageable pageable);
}
