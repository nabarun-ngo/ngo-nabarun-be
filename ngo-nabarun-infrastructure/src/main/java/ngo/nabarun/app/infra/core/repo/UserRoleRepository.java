package ngo.nabarun.app.infra.core.repo;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import ngo.nabarun.app.infra.core.entity.UserRoleEntity;

@Deprecated
public interface UserRoleRepository extends MongoRepository<UserRoleEntity,String> {
	
	List<UserRoleEntity> findByProfileIdAndActiveTrue(String id);
	List<UserRoleEntity> findByUserIdAndActiveTrue(String id);
	List<UserRoleEntity> findByProfileId(String id);
	List<UserRoleEntity> findByUserId(String id);

}
