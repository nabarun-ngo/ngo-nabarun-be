package ngo.nabarun.app.infra.core.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.UserContactEntity;

@Repository
public interface UserContactRepository extends MongoRepository<UserContactEntity,String>{
	
	List<UserContactEntity> findByPhoneNumber(String phoneNumber);

}
