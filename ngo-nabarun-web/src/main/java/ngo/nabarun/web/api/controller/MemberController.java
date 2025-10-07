
package ngo.nabarun.web.api.controller;

import ngo.nabarun.application.dto.result.MemberResponse;
import ngo.nabarun.application.mapper.MemberDomainMapper;
import ngo.nabarun.application.service.MemberService;
import ngo.nabarun.domain.member.Member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {
	
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> get(@PathVariable String id) {
        Optional<Member> m = memberService.findById(id);
        return m.map(member -> ResponseEntity.ok(MemberDomainMapper.toResponse(member)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<MemberResponse> list() {
        return MemberDomainMapper.toResponses(memberService.findAll());
    }

    @PostMapping
    public MemberResponse create(@RequestBody MemberResponse req) {
        Member domain = new Member(req.getId(), req.getName(), req.getEmail(), req.getJoinedAt() == null ? Instant.now() : req.getJoinedAt());
        Member saved = memberService.create(domain);
        return MemberDomainMapper.toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
