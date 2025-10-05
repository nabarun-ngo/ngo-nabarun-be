
package ngo.nabarun.infra.config;

import ngo.nabarun.application.service.MemberService;
import ngo.nabarun.domain.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public MemberService memberService(MemberRepository memberRepository) {
        return new MemberService(memberRepository);
    }
}
