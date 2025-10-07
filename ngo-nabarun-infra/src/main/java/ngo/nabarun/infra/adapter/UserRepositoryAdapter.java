package ngo.nabarun.infra.adapter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.repository.UserRepositoryPort;
import ngo.nabarun.infra.mapper.InfraUserMapper;
import ngo.nabarun.infra.mongo.entity.UserEntity;
import ngo.nabarun.infra.mongo.repo.UserMongoRepository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort{
	
	@Autowired
	private UserMongoRepository userRepo;
	
	@Autowired
	private InfraUserMapper userMapper;
	
	
	@Override
	public User createUser(User user) {
		UserEntity userEntity=userMapper.toEntity(user);
		System.out.println(userEntity);
		userEntity=userRepo.save(userEntity);
		return userMapper.toDomain(userEntity);
	}

	@Override
	public User updateUser(User user) {
		UserEntity userEntity=userMapper.toEntity(user);
		userEntity=userRepo.save(userEntity);
		return userMapper.toDomain(userEntity);
	}

	@Override
	public boolean isUserExists(String email) {
		return userRepo.findByEmail(email).isPresent();
	}

	@Override
	public User assignRolesToUser(User user, List<Role> rolesToAdd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User deleteRolesFromUser(User user, List<Role> rolesToDelete) {
		// TODO Auto-generated method stub
		return null;
	}

}
