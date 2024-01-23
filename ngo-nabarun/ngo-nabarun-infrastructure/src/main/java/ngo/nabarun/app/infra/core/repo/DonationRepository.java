package ngo.nabarun.app.infra.core.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ngo.nabarun.app.infra.core.entity.DonationEntity;


public interface DonationRepository extends MongoRepository<DonationEntity,String>,QuerydslPredicateExecutor<DonationEntity> {
	
	
	long countByProfile(String profileId);

	@Query("{deleted: true}")
	List<DonationEntity> findAllDeletedContribution();
	
	@Query("{isGuest:true,deleted:false}")
	List<DonationEntity> findAllGuestContribution();


}
