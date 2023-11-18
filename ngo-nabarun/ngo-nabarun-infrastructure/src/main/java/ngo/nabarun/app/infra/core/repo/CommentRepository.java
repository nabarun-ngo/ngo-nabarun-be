package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ngo.nabarun.app.infra.core.entity.Comment;

@Repository
public interface CommentRepository extends MongoRepository<Comment,String>{

}
