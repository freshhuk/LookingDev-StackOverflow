package com.lookingdev.stackoverflow.Repository;

import com.lookingdev.stackoverflow.Domain.Entities.DeveloperProfile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DevelopersRepository extends MongoRepository<DeveloperProfile, ObjectId> {

    DeveloperProfile findByUsername(String name);
}
