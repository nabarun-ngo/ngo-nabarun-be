
package ngo.nabarun.application.mapper;

import ngo.nabarun.application.dto.result.MemberResponse;
import ngo.nabarun.domain.member.Member;

import java.util.stream.Collectors;
import java.util.List;

public class MemberDomainMapper {
    public static MemberResponse toResponse(Member m) {
        if (m == null) return null;
        return new MemberResponse(m.getId(), m.getName(), m.getEmail(), m.getJoinedAt());
    }
    public static List<MemberResponse> toResponses(List<Member> list) {
        return list.stream().map(MemberDomainMapper::toResponse).collect(Collectors.toList());
    }
}
