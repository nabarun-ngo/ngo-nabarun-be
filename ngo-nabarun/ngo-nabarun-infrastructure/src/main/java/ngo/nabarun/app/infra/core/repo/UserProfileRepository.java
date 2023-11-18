package ngo.nabarun.app.infra.core.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.UserProfileEntity;

public interface UserProfileRepository extends MongoRepository<UserProfileEntity,String>{
	
	Optional<UserProfileEntity> findByUserIdContaining(String userId);
	Optional<UserProfileEntity> findByEmail(String email);
	List<UserProfileEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserId(String firstName,String lastName,String email,String userId);

//	@Query("{'$or':[ {firstName: ?0 }, {lastName: ?0},{ userId : ?0 }, {profileStatus : ?0} ] }")
//	List<UserProfileEntity> findByQuery(String query);
}
