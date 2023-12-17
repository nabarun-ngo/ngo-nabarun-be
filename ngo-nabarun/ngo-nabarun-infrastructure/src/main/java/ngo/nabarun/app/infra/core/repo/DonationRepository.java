package ngo.nabarun.app.infra.core.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.DonationEntity;


@Repository
public interface DonationRepository extends MongoRepository<DonationEntity,String>{
	
	@Query("{profile:?0}") 
	List<DonationEntity> findByProfileId(@Param("profileId") String profileId);
	Page<DonationEntity> findByProfile(String profileId,Pageable pageable);
	long countByProfile(String profileId);

	@Query("{deleted: true}")
	List<DonationEntity> findAllDeletedContribution();
	
	@Query("{isGuest:true,deleted:false}")
	List<DonationEntity> findAllGuestContribution();
}
