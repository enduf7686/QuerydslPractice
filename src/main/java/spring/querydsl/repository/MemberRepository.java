package spring.querydsl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.querydsl.entity.Member;


public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

    Optional<Member> findByUsername(String username);
}
