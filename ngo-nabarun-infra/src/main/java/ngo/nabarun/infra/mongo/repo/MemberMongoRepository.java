
package ngo.nabarun.infra.mongo.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.infra.mongo.entity.MemberEntity;

@Repository
public interface MemberMongoRepository extends MongoRepository<MemberEntity, String> {
}
