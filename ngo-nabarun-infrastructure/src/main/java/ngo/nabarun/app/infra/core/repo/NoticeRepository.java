package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import ngo.nabarun.app.infra.core.entity.NoticeEntity;


public interface NoticeRepository extends MongoRepository<NoticeEntity,String>{

	@Query("{createdBy: ?0, draft: true}") 
	List<NoticeEntity> findDraftedNotice(@Param("userId") String userId);
	
	@Query("{deleted: true}") 
	List<NoticeEntity> findAllDeletedNotice();

}
