package ngo.nabarun.infra.adapter.ext;

import org.springframework.stereotype.Service;

import ngo.nabarun.application.port.IAMPort;
import ngo.nabarun.domain.user.model.User;

@Service
public class Auth0Adapter implements IAMPort{

	@Override
	public User createUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

}
