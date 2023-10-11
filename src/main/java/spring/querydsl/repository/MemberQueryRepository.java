package spring.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.querydsl.dto.MemberSearchCond;
import spring.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberQueryRepository {

    List<MemberTeamDto> search(MemberSearchCond cond);

    Page<MemberTeamDto> searchPage(MemberSearchCond cond, Pageable pageable);
}
