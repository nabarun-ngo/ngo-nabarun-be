package ngo.nabarun.infra.mongo.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.infra.mongo.entity.UserEntity;

public interface UserMongoRepository  extends MongoRepository<UserEntity, String> {

	Optional<UserEntity> findByEmail(String email);

}

