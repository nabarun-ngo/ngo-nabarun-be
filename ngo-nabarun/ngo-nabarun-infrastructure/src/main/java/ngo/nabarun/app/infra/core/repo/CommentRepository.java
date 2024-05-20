package ngo.nabarun.app.infra.core.repo;


import org.springframework.data.mongodb.repository.MongoRepository;

import ngo.nabarun.app.infra.core.entity.Comment;

public interface CommentRepository extends MongoRepository<Comment,String>{

}
