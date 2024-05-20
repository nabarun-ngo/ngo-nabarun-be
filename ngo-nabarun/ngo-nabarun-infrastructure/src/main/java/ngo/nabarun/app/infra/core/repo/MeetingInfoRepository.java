package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.MeetingEntity;

public interface MeetingInfoRepository extends MongoRepository<MeetingEntity,String>{

	
}
