package com.lookingdev.stackoverflow.Repository;

import com.lookingdev.stackoverflow.Domain.Entities.DeveloperProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DevelopersRepository extends MongoRepository<DeveloperProfile, Integer> {

    @Query("{ 'id': { $gt: ?1 } }")
    List<DeveloperProfile> findDevelopersWithLimit(int lastId, Pageable pageable);
}
