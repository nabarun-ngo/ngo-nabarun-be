package ngo.nabarun.infra.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.infra.mongo.entity.UserRoleEntity;

public interface UserRoleMongoRepository  extends MongoRepository<UserRoleEntity, String> {

}

