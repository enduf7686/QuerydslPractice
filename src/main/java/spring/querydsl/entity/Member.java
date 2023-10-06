package spring.querydsl.entity;

import lombok.*;

import javax.persistence.*;
import java.time.temporal.TemporalAmount;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        this.team = team;
    }
}
