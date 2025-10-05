
package ngo.nabarun.infra.adapter;

import ngo.nabarun.domain.model.Member;
import ngo.nabarun.domain.repository.MemberRepository;
import ngo.nabarun.infra.mongo.entity.MemberEntity;
import ngo.nabarun.infra.mongo.repo.MemberMongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MemberMongoAdapter implements MemberRepository {

    private final MemberMongoRepository repo;

    public MemberMongoAdapter(MemberMongoRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Member> findById(String id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Member> findAll() {
        return repo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Member save(Member member) {
        MemberEntity e = toEntity(member);
        MemberEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
    }

    private Member toDomain(MemberEntity e) {
        return new Member(e.getId(), e.getName(), e.getEmail(), e.getJoinedAt());
    }

    private MemberEntity toEntity(Member m) {
        return new MemberEntity(m.getId(), m.getName(), m.getEmail(), m.getJoinedAt());
    }
}
