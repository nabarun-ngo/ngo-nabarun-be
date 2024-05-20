package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.MeetingAdditionalInfoEntity;

public interface MeetingAdditionalInfoRepository extends MongoRepository<MeetingAdditionalInfoEntity,String>{

	
}
