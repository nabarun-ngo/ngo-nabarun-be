package ngo.nabarun.application.port;

import ngo.nabarun.domain.user.model.User;

public interface IAMPort {
	User createUser(User user);
}
