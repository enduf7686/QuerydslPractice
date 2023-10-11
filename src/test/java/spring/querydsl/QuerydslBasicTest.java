package spring.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import spring.querydsl.dto.MemberDto;
import spring.querydsl.dto.QMemberDto;
import spring.querydsl.dto.UserDto;
import spring.querydsl.entity.Member;
import spring.querydsl.entity.QMember;
import spring.querydsl.entity.Team;
import spring.querydsl.repository.MemberRepository;
import spring.querydsl.repository.TeamRepository;

import javax.persistence.EntityManager;

import java.util.List;

import static spring.querydsl.entity.QMember.*;
import static spring.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void init() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamA));
        memberRepository.save(new Member("member3", 30, teamB));
        memberRepository.save(new Member("member4", 40, teamB));

        em.flush();
        em.clear();
    }

    @Test
    void joinOnFiltering() {
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamB"))
                .fetch();

        result.stream()
                .forEach(tuple -> System.out.println(tuple));
    }

    @Test
    void joinOnNoRelation() {
        memberRepository.save(Member.builder().username("teamA").build());
        memberRepository.save(Member.builder().username("teamB").build());
        memberRepository.save(Member.builder().username("teamA").build());

        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .join(team)
                .on(member.username.eq(team.name))
                .fetch();

        result.stream()
                .forEach(tuple -> System.out.println(tuple));
    }

    @Test
    void noFetchJoin() {
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        result.stream()
                .forEach(member -> System.out.println(member.getTeam().getName()));
    }

    @Test
    void fetchJoin() {
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .fetch();

        result.stream()
                .forEach(member -> System.out.println(member.getTeam().getName()));
    }

    @Test
    void subQuery1() {
        QMember memberSub = new QMember("memberSub");

        Member result = queryFactory.select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetchOne();

        System.out.println(result);
    }

    @Test
    void subQuery2() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.select(member)
                .from(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        result.stream()
                .forEach(member -> System.out.println(member));
    }

    @Test
    void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열 살")
                        .when(20).then("스무 살")
                        .otherwise("서른 살 이상"))
                .from(member)
                .fetch();

        result.stream()
                .forEach(s -> System.out.println(s));
    }

    @Test
    void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 ~ 20")
                        .otherwise("20 초과")
                )
                .from(member)
                .fetch();

        result.stream()
                .forEach(s -> System.out.println(s));
    }

    @Test
    void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        result.stream()
                .forEach(tuple -> System.out.println(tuple));
    }

    @Test
    void concat() {
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.in("member1", "member2"))
                .fetch();

        result.stream()
                .forEach(s -> System.out.println(s));
    }

    @Test
    void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        result.stream()
                .forEach(s -> System.out.println(s));
    }

    @Test
    void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        result.stream()
                .forEach(tuple -> {
                    System.out.println(tuple.get(member.username));
                    System.out.println(tuple.get(member.age));
                });
    }

    @Test
    void findDtoByJpql() {
        List<MemberDto> resultList = em.createQuery("select new spring.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        resultList.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void findDtoByQuerydsl1() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void findDtoByQuerydsl2() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void findDtoByQuerydsl3() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void findDtoByQuerydsl4() {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        JPAExpressions
                                .select(member.age.max())
                                .from(member)))
                .from(member)
                .fetch();

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    @Test
    void dynamicQueryBooleanBuilder() {
        String usernameParam = "member2";
        Integer ageParam = 20;

        List<Member> result = searchMember1(usernameParam, ageParam);

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQueryWhereParam() {
        String usernameParam = "member2";
        Integer ageParam = 20;

        List<Member> result = searchMember2(usernameParam, ageParam);

        result.stream()
                .forEach(m -> System.out.println(m));
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    @Test
    void bulkUpdate() {
        queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(25))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        result.stream().forEach(m -> System.out.println(m));
    }

    @Test
    void bulkAdd() {
        queryFactory
                .update(member)
                .set(member.age, member.age.multiply(2))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        result.stream().forEach(m -> System.out.println(m));
    }

    @Test
    void bulkDelete() {
        queryFactory
                .delete(member)
                .where(member.age.lt(18))
                .execute();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        result.stream().forEach(m -> System.out.println(m));
    }

    @Test
    void sqlFunction1() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})", member.username, "member", "M")
                )
                .from(member)
                .fetch();

        result.stream().forEach(m -> System.out.println(m));
    }

    @Test
    void sqlFunction2() {
        List<String> result = queryFactory
                .select(member.username.upper()) //ANSI 표준에 있는 함수들은 Querydsl에서 대부분 구현 해놓았다.
                .from(member)
                .fetch();

        result.stream().forEach(m -> System.out.println(m));
    }
}
