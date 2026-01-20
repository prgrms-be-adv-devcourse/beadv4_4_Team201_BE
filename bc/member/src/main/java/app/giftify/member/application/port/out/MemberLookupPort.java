package app.giftify.member.application.port.out;

import java.util.Optional;

import app.giftify.shared.domain.vo.MemberInfo;

/**
 * 인증된 사용자 정보를 조회하는 포트.
 */
public interface MemberLookupPort {
	Optional<MemberInfo> findByAuthSub(String authSub);
}
