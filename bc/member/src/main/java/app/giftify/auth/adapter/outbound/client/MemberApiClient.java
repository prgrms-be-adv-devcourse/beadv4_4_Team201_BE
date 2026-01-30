package app.giftify.auth.adapter.outbound.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import app.giftify.shared.domain.vo.MemberInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * Member 서비스와 통신하는 API 클라이언트.
 * <p>모놀리스 환경에서는 localhost로 호출</p>
 */
@Slf4j
@Component
public class MemberApiClient {

	private final RestClient restClient;

	public MemberApiClient(
		RestClient.Builder restClientBuilder,
		@Value("${app.service.member.url:http://localhost:8080}") String memberServiceUrl) {
		this.restClient = restClientBuilder
			.baseUrl(memberServiceUrl)
			.build();
	}

	/**
	 * Auth0 식별자로 회원 정보를 조회한다.
	 *
	 * @param authSub Auth0 고유 식별자 (JWT subject)
	 * @return 회원 정보, 미등록 사용자인 경우 empty
	 */
	public Optional<MemberInfo> getMemberByAuthSub(String authSub) {
		try {
			MemberInfo response = restClient.get()
				.uri("/api/internal/members/by-auth-sub/{authSub}", authSub)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
					// 404 등 클라이언트 에러는 empty로 처리
				})
				.body(MemberInfo.class);

			return Optional.ofNullable(response);
		} catch (Exception e) {
			log.warn("Failed to fetch member info for authSub: {}", authSub, e);
			return Optional.empty();
		}
	}
}
