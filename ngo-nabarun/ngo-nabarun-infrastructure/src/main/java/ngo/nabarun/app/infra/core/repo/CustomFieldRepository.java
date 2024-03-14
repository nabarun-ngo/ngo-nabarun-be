package ngo.nabarun.app.infra.core.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;

public interface CustomFieldRepository extends MongoRepository<CustomFieldEntity,String>{
	Optional<CustomFieldEntity> findBySourceAndFieldKey(String source,String fieldKey);
}
