package ngo.nabarun.application.port;

import ngo.nabarun.domain.user.model.User;

public interface MailPort {
    void sendOnboardingEmail(User user);
}
