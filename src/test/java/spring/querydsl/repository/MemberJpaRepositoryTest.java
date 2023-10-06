package spring.querydsl.repository;

import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10, null);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        memberJpaRepository.save(new Member("member1", 10, teamA));
        memberJpaRepository.save(new Member("member2", 20, teamA));
        memberJpaRepository.save(new Member("member3", 30, teamB));
        memberJpaRepository.save(new Member("member4", 40, teamB));

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByWhere(cond);
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    void searchMemberTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        memberJpaRepository.save(new Member("member1", 10, teamA));
        memberJpaRepository.save(new Member("member2", 20, teamA));
        memberJpaRepository.save(new Member("member3", 30, teamB));
        memberJpaRepository.save(new Member("member4", 40, teamB));

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(40);
        cond.setTeamName("teamB");

        List<Member> result = memberJpaRepository.searchMember(cond);
        result.stream().forEach(m -> System.out.println(m));
        assertThat(result).extracting("username").containsExactly("member4");
    }
}