package app.giftify.auth.adapter.outbound.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import app.giftify.shared.domain.vo.MemberInfo;

/**
 * Member Internal API 클라이언트.
 * Auth 모듈에서 Member 모듈의 내부 API를 호출할 때 사용합니다.
 */
public interface MemberApiClient {

    @GetExchange("/api/internal/members/by-auth-sub")
    ResponseEntity<MemberInfo> getMemberByAuthSub(@RequestParam("authSub") String authSub);

    @PostExchange("/api/internal/members")
    MemberInfo createMember(@RequestBody CreateMemberRequest request);

    record CreateMemberRequest(String authSub, String email, String name) {}
}
