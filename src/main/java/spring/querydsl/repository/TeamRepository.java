package spring.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.querydsl.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
