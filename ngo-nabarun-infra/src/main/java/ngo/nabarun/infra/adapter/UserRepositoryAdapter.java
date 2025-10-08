package ngo.nabarun.infra.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.port.UserRepositoryPort;
import ngo.nabarun.infra.mapper.InfraUserMapper;
import ngo.nabarun.infra.mongo.entity.UserEntity;
import ngo.nabarun.infra.mongo.entity.UserRoleEntity;
import ngo.nabarun.infra.mongo.repo.UserMongoRepository;
import ngo.nabarun.infra.mongo.repo.UserRoleMongoRepository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort{
    
    @Autowired
    private UserMongoRepository userRepo;
    
    @Autowired
    private UserRoleMongoRepository userRoleRepo;
    
    @Autowired
    private InfraUserMapper userMapper;
    
    @Override
    public User createUser(User user) {
        UserEntity userEntity=userMapper.toEntity(user);
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
    public Optional<User> findById(String id) {
        return userRepo.findById(id).map(userMapper::toDomain);
    }

    @Override
    public List<User> findPage(String emailLike, UserStatus status, String roleCode, int page, int size, String sort) {
        Sort s = (sort == null || sort.isBlank()) ? Sort.by(Sort.Direction.DESC, "createdOn") : Sort.by(sort);
        Pageable pageable = PageRequest.of(page, size, s);
        Page<UserEntity> entities = userRepo.findAll(pageable);
        List<UserEntity> filtered = entities.getContent().stream()
            .filter(e -> emailLike == null || (e.getEmail() != null && e.getEmail().toLowerCase().contains(emailLike.toLowerCase())))
            .filter(e -> status == null || (e.getStatus() != null && e.getStatus().equals(status.name())))
            .filter(e -> roleCode == null || (e.getRoleCodes() != null && e.getRoleCodes().contains(roleCode)))
            .collect(Collectors.toList());
        return filtered.stream().map(userMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count(String emailLike, UserStatus status, String roleCode) {
        List<UserEntity> all = new ArrayList<>();
        userRepo.findAll().forEach(all::add);
        return all.stream()
            .filter(e -> emailLike == null || (e.getEmail() != null && e.getEmail().toLowerCase().contains(emailLike.toLowerCase())))
            .filter(e -> status == null || (e.getStatus() != null && e.getStatus().equals(status.name())))
            .filter(e -> roleCode == null || (e.getRoleCodes() != null && e.getRoleCodes().contains(roleCode)))
            .count();
    }

    @Override
    public User assignRolesToUser(User user, List<Role> rolesToAdd) {
        if (rolesToAdd == null || rolesToAdd.isEmpty()) return user;
        for(Role role:rolesToAdd) {
        	UserRoleEntity entity = new UserRoleEntity();
        	entity.setActive(true);
        	entity.setCreatedBy("System");
        	entity.setCreatedOn(new Date());
        	entity.setId(UUID.randomUUID().toString());
        	entity.setProfileId(user.getId());
        	entity.setRoleCode(role.roleCode());
        	entity.setRoleDisplayName(role.roleDisplayName());
        	entity.setRoleName(role.roleName());
        	entity.setUserId(user.getUserId());
        	userRoleRepo.save(entity);
        }
        return user;
    }

    @Override
    public User deleteRolesFromUser(User user, List<Role> rolesToDelete) {
        if (rolesToDelete == null || rolesToDelete.isEmpty()) return user;
        return user;
    }

}
