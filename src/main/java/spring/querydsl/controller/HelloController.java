package spring.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import spring.querydsl.entity.Member;
import spring.querydsl.repository.MemberRepository;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HelloController {

    private final MemberRepository memberRepository;

    @GetMapping("/save")
    @ResponseBody
    public Page<Member> save(@RequestParam String name, @RequestParam int age, Pageable pageable) {
        memberRepository.save(new Member(name, age, null));

        return memberRepository.findAll(pageable);
    }
}
