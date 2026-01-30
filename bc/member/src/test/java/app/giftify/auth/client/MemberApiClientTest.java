package app.giftify.auth.client;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MemberApiClient")
class MemberApiClientTest {

    private MemberApiClient memberApiClient;
    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        restClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        RestClient.Builder builder = mock(RestClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        memberApiClient = new MemberApiClient(builder, "http://localhost:8080");
    }

    @Nested
    @DisplayName("getMemberByAuthSub")
    class GetMemberByAuthSub {

        @Test
        @DisplayName("회원이 존재하면 MemberInfo를 Optional로 반환한다")
        void returnsMemberInfoWhenExists() {
            // given
            String authSub = "auth0|existing-user";
            MemberInfo expectedMember = MemberInfo.of(
                    1L, authSub, MemberRole.BUYER, "test@test.com", "tester"
            );

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString(), eq(authSub))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.body(MemberInfo.class)).thenReturn(expectedMember);

            // when
            Optional<MemberInfo> result = memberApiClient.getMemberByAuthSub(authSub);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().memberId()).isEqualTo(1L);
            assertThat(result.get().authSub()).isEqualTo(authSub);
            assertThat(result.get().role()).isEqualTo(MemberRole.BUYER);
        }

        @Test
        @DisplayName("회원이 없으면 (body가 null) Optional.empty()를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            String authSub = "auth0|non-existing-user";

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString(), eq(authSub))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.body(MemberInfo.class)).thenReturn(null);

            // when
            Optional<MemberInfo> result = memberApiClient.getMemberByAuthSub(authSub);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("예외 발생 시 Optional.empty()를 반환한다")
        void returnsEmptyWhenExceptionOccurs() {
            // given
            String authSub = "auth0|error-user";

            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString(), eq(authSub))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection error"));

            // when
            Optional<MemberInfo> result = memberApiClient.getMemberByAuthSub(authSub);

            // then
            assertThat(result).isEmpty();
        }
    }
}
