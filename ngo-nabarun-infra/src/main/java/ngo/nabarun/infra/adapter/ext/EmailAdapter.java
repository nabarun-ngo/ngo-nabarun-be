package ngo.nabarun.infra.adapter.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ngo.nabarun.application.port.MailPort;
import ngo.nabarun.domain.user.model.User;

@Service
public class EmailAdapter implements MailPort {
    private static final Logger log = LoggerFactory.getLogger(EmailAdapter.class);

    @Override
    public void sendOnboardingEmail(User user) {
        log.info("[Email] Sent onboarding email to {}", user.getEmail());
    }
}
