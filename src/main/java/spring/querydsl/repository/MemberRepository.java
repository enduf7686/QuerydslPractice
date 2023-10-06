package spring.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.querydsl.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
