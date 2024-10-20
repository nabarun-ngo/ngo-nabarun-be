package ngo.nabarun.app.infra.core.repo;


import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.DashboardCountEntity;

public interface DashboardCountRepository extends MongoRepository<DashboardCountEntity,String>{
	List<DashboardCountEntity> findByUserIdIn(List<String> userId);
}
