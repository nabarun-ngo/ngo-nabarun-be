package ngo.nabarun.application.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.application.mapper.AppUserMapper;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.domain.BusinessException;
import ngo.nabarun.domain.BusinessException.ExceptionEvent;
import ngo.nabarun.domain.user.event.UserCreatedEvent;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.model.User.UserCreate;
import ngo.nabarun.domain.user.repository.UserRepositoryPort;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserRepositoryPort userRepository;
	
	@Autowired
	private AppUserMapper mapper;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Transactional
	@Override
	public UserResult createUser(UserCreateCommand command) {
		UserCreate create= mapper.toCreateDomain(command);
		User user = new User(create);
		if(userRepository.isUserExists(user.getEmail())) {
			throw new BusinessException(ExceptionEvent.EMAIL_ALREADY_IN_USE);
		}
		user=userRepository.createUser(user);
		eventPublisher.publishEvent(new UserCreatedEvent(user));
		
		return mapper.toResult(user);
	}
	
	

}
