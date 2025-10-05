
package ngo.nabarun.web.controller;

import ngo.nabarun.application.dto.MemberResponse;
import ngo.nabarun.application.mapper.MemberMapper;
import ngo.nabarun.application.service.MemberService;
import ngo.nabarun.domain.model.Member;
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
        return m.map(member -> ResponseEntity.ok(MemberMapper.toResponse(member)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<MemberResponse> list() {
        return MemberMapper.toResponses(memberService.findAll());
    }

    @PostMapping
    public MemberResponse create(@RequestBody MemberResponse req) {
        Member domain = new Member(req.getId(), req.getName(), req.getEmail(), req.getJoinedAt() == null ? Instant.now() : req.getJoinedAt());
        Member saved = memberService.create(domain);
        return MemberMapper.toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
