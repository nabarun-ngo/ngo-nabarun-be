package ngo.nabarun.app.infra.core.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;

public interface CustomFieldRepository extends MongoRepository<CustomFieldEntity,String>{
	
	//List<UserContactEntity> findByPhoneNumber(String phoneNumber);

}
