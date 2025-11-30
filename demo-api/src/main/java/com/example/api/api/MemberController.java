package com.example.api.api;

import com.example.member.application.MemberCommandService;
import com.example.member.application.MemberActivationCommand;
import com.example.member.application.MemberActivationService;
import com.example.member.application.MemberQueryService;
import com.example.member.application.MemberRegisterCommand;
import com.example.member.domain.Member;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final MemberActivationService memberActivationService;

    public MemberController(
            MemberCommandService memberCommandService,
            MemberQueryService memberQueryService,
            MemberActivationService memberActivationService
    ) {
        this.memberCommandService = memberCommandService;
        this.memberQueryService = memberQueryService;
        this.memberActivationService = memberActivationService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> register(@Valid @RequestBody CreateMemberRequest request) {
        Member member = memberCommandService.register(
                new MemberRegisterCommand(request.email(), request.name(), request.password()));
        return ResponseEntity.ok(
                new MemberResponse(
                        member.getId(),
                        member.getEmail().getValue(),
                        member.getName(),
                        member.getStatus(),
                        member.getRoles(),
                        member.getLastLoginAt()
                ));
    }

    @PostMapping("/activate")
    public ResponseEntity<MemberResponse> activate(@Valid @RequestBody ActivateMemberRequest request) {
        Member member = memberActivationService.activate(
                new MemberActivationCommand(request.email(), request.code()));
        return ResponseEntity.ok(MemberResponse.from(member));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public MemberResponse get(@PathVariable Long id) {
        return MemberResponse.from(memberQueryService.getById(id));
    }
}
