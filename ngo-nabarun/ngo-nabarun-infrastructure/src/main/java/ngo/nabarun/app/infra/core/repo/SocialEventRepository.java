package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.SocialEventEntity;

@Repository
public interface SocialEventRepository extends MongoRepository<SocialEventEntity,String>{
	@Query("{createdBy: ?0, draft: true}")
	List<SocialEventEntity> findDraftedEventByCreator(@Param("userId") String userId);

	@Query("{deleted: true}")
	List<SocialEventEntity> findAllDeletedEvent();

}
