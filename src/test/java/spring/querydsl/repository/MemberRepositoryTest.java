package spring.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import spring.querydsl.dto.MemberSearchCond;
import spring.querydsl.dto.MemberTeamDto;
import spring.querydsl.entity.Member;
import spring.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10, null);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).orElse(null);
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        Optional<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).isPresent();
        assertThat(result2.get()).isEqualTo(member);
    }

    @Test
    void searchMemberTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamA));
        memberRepository.save(new Member("member3", 30, teamB));
        memberRepository.save(new Member("member4", 40, teamB));

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(cond);
        result.stream().forEach(m -> System.out.println(m));
        assertThat(result).extracting("username").containsExactly("member4");
    }
}