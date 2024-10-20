package ngo.nabarun.app.infra.core.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.ApiKeyEntity;

public interface ApiKeyRepository extends MongoRepository<ApiKeyEntity,String>{
	Optional<ApiKeyEntity> findByApiKey(String apiKey);

	List<ApiKeyEntity> findByStatus(String status);

}
