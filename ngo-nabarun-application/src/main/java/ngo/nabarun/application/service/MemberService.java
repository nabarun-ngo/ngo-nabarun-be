
package ngo.nabarun.application.service;

import ngo.nabarun.common.props.PropertyHelper;
import ngo.nabarun.domain.member.Member;
import ngo.nabarun.domain.member.MemberRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    
    @Autowired
	private PropertyHelper propertyHelper;


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
