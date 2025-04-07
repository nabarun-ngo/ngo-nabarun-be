package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ngo.nabarun.app.infra.core.entity.SocialEventEntity;


public interface SocialEventRepository extends MongoRepository<SocialEventEntity,String>,QuerydslPredicateExecutor<SocialEventEntity>{
	/*
	@Query("{createdBy: ?0, draft: true}")
	List<SocialEventEntity> findDraftedEventByCreator(@Param("userId") String userId);

	@Query("{deleted: true}")
	List<SocialEventEntity> findAllDeletedEvent();
	*/
}
