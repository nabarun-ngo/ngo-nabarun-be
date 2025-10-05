
package ngo.nabarun.application.service;

import ngo.nabarun.domain.model.Member;
import ngo.nabarun.domain.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Optional<Member> findById(String id) {
        return memberRepository.findById(id);
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member create(Member m) {
        return memberRepository.save(m);
    }

    public void delete(String id) {
        memberRepository.deleteById(id);
    }
}
