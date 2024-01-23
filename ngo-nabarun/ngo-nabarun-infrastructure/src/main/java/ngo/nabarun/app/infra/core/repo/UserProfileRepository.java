package ngo.nabarun.app.infra.core.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ngo.nabarun.app.infra.core.entity.UserProfileEntity;

public interface UserProfileRepository extends MongoRepository<UserProfileEntity,String>,QuerydslPredicateExecutor<UserProfileEntity> {
	
	Optional<UserProfileEntity> findByUserId(String userId);
	Optional<UserProfileEntity> findByEmail(String email);

}
