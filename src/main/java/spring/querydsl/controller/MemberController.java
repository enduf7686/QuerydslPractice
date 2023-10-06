package spring.querydsl.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import spring.querydsl.dto.MemberSearchCond;
import spring.querydsl.dto.MemberTeamDto;
import spring.querydsl.repository.MemberJpaRepository;
import spring.querydsl.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(@ModelAttribute MemberSearchCond cond) {
        return memberJpaRepository.searchByWhere(cond);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(@ModelAttribute MemberSearchCond cond, @PageableDefault Pageable pageable) {
        return memberRepository.searchPage(cond, pageable);
    }
}
