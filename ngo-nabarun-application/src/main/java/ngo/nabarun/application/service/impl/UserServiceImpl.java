package ngo.nabarun.application.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserSelfUpdateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.application.mapper.AppUserMapper;
import ngo.nabarun.application.port.IAMPort;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.domain.BusinessException;
import ngo.nabarun.domain.BusinessException.ExceptionEvent;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.event.UserCreatedEvent;
import ngo.nabarun.domain.user.event.UserUpdatedEvent;
import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.port.RoleMetadataPort;
import ngo.nabarun.domain.user.port.UserRepositoryPort;
import ngo.nabarun.domain.user.vo.RoleUpdateResult;
import ngo.nabarun.event.publisher.AppEventPublisher;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepositoryPort userRepositoryPort;

	@Autowired
	private RoleMetadataPort roleMetadataPort;

	@Autowired
	private AppUserMapper mapper;

	@Autowired
	private IAMPort iamPort;

	@Autowired
	private AppEventPublisher eventPublisher;

	@Override
	@Transactional
	public UserResult createUser(UserCreateCommand command) {
		var create = mapper.toRegistrationDetail(command);
		User user = new User(create);
		if (userRepositoryPort.isUserExists(user.getEmail())) {
			throw new BusinessException(ExceptionEvent.EMAIL_ALREADY_IN_USE);
		}
		userRepositoryPort.createUser(user);
		User iamUser = iamPort.createUser(user, command.isEmailverified());
		eventPublisher.publishEvent(new UserCreatedEvent(user.getId(), iamUser.getId()));
		return mapper.toUserResult(user);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResult<UserResult> listUsers(String emailLike, UserStatus status, String roleCode, int page, int size,
			String sort) {
		List<User> users = userRepositoryPort.findPage(emailLike, status, roleCode, page, size, sort);
		long total = userRepositoryPort.count(emailLike, status, roleCode);
		List<UserResult> results = users.stream().map(mapper::toUserResult).collect(Collectors.toList());
		return new PagedResult<>(results, total, page, size);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserResult> getUser(String id) {
		return userRepositoryPort.findById(id).map(mapper::toUserResult);
	}

	@Override
	@Transactional
	public UserResult updateMyDetails(String id, UserSelfUpdateCommand command) {
		User user = userRepositoryPort.findById(id).orElseThrow(() -> new BusinessException("User not found"));
		var update = mapper.toProfileUpdate(command);
		user.updateUser(update);
		userRepositoryPort.updateUser(user);
		eventPublisher.publishEvent(new UserUpdatedEvent(user.getId(), user.getUserId(), update, null, null));
		return mapper.toUserResult(user);
	}

	@Override
	@Transactional
	public UserResult updateUser(String id, UserUpdateCommand command) {
		User user = userRepositoryPort.findById(id).orElseThrow(() -> new BusinessException("User not found"));
		var update = mapper.toAdminUpdate(command);
		user.updateUser(update);
		userRepositoryPort.updateUser(user);
		RoleUpdateResult result = null;
		if (command.getRoleCodes() != null) {
			List<Role> allRoles = roleMetadataPort.getAllRoles();
			List<Role> newRoles = allRoles.stream().filter(r -> command.getRoleCodes().contains(r.roleCode()))
					.toList();
			result = user.updateRoles(newRoles, roleMetadataPort.getDefaultRoles());
			if (result.hasChanges()) {
				userRepositoryPort.assignRolesToUser(user, result.rolesToAdd());
				userRepositoryPort.deleteRolesFromUser(user, result.rolesToRemove());
			}
		}
		eventPublisher.publishEvent(new UserUpdatedEvent(user.getId(), user.getUserId(), null, update, result));
		return mapper.toUserResult(user);
	}
}
