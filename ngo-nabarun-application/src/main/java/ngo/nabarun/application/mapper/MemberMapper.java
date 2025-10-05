
package ngo.nabarun.application.mapper;

import ngo.nabarun.domain.model.Member;
import ngo.nabarun.application.dto.MemberResponse;
import java.util.stream.Collectors;
import java.util.List;

public class MemberMapper {
    public static MemberResponse toResponse(Member m) {
        if (m == null) return null;
        return new MemberResponse(m.getId(), m.getName(), m.getEmail(), m.getJoinedAt());
    }
    public static List<MemberResponse> toResponses(List<Member> list) {
        return list.stream().map(MemberMapper::toResponse).collect(Collectors.toList());
    }
}
