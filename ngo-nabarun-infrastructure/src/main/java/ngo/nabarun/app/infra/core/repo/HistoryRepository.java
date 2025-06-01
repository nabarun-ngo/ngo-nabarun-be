package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.HistoryEntity;

public interface HistoryRepository extends MongoRepository<HistoryEntity,String>{
	List<HistoryEntity> findByReferenceIdAndReferenceType(String refId,String type);
}
