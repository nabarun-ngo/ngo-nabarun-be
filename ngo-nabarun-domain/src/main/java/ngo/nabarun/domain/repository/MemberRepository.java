
package ngo.nabarun.domain.repository;

import ngo.nabarun.domain.model.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(String id);
    List<Member> findAll();
    Member save(Member member);
    void deleteById(String id);
}
