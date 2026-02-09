package app.giftify.replica.member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	// nickname 파라미터가 있을 때 닉네임 부분 일치 검색
	List<Member> findByNicknameContainingIgnoreCase(String nickname);
}
