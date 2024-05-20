package ngo.nabarun.app.infra.core.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;


public interface TicketRepository extends MongoRepository<TicketInfoEntity, String> {

//	@Query(value = "select * from otps where identifier=:identifier", nativeQuery = true)
	//List<TicketInfoEntity> findByIdentifier(@Param("identifier") String identifier);
	Optional<TicketInfoEntity> findByToken(String token);


//	@Modifying
//	@Query("UPDATE otps SET incorrect_attempts= incorrect_attempts+1 where id = :id")
//	void incrementIncorrectAttempt(@Param(value = "id") String id);
	
//	@Transactional
//	@Modifying
//	@Query(value= "UPDATE otps SET incorrect_attempts = incorrect_attempts+1 WHERE id=:id", nativeQuery = true )
//	void incrementIncorrectAttempt(@Param("id") String id);
}
