package app.giftify.member.adapter.out.jpa.adapter;

import app.giftify.member.adapter.out.jpa.entity.MemberJpaEntity;
import app.giftify.member.adapter.out.jpa.respository.MemberJpaRepository;
import app.giftify.member.domain.member.Member;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.support.common.security.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberPersistenceAdapterTest {

    @Mock
    private MemberJpaRepository memberJpaRepository;

    @InjectMocks
    private MemberPersistenceAdapter memberPersistenceAdapter;

    @Test
    @DisplayName("[영속성 어댑터] AuthSub으로 회원 조회")
    void findByAuthSub() {
        // given
        String authSub = "auth0|123";
        MemberJpaEntity entity = MemberJpaEntity.builder()
                .authSub(authSub)
                .email("test@test.com")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberJpaRepository.findByAuthSub(authSub)).willReturn(Optional.of(entity));

        // when
        Optional<Member> result = memberPersistenceAdapter.findByAuthSub(authSub);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAuthSub()).isEqualTo(authSub);
    }

    @Test
    @DisplayName("[영속성 어댑터] ID로 회원 조회")
    void findById() {
        // given
        Long id = 1L;
        MemberJpaEntity entity = MemberJpaEntity.builder()
                .id(id)
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberJpaRepository.findById(id)).willReturn(Optional.of(entity));

        // when
        Optional<Member> result = memberPersistenceAdapter.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("[영속성 어댑터] 이메일로 회원 조회")
    void findByEmail() {
        // given
        String email = "test@test.com";
        MemberJpaEntity entity = MemberJpaEntity.builder()
                .email(email)
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberJpaRepository.findByEmail(email)).willReturn(Optional.of(entity));

        // when
        Optional<Member> result = memberPersistenceAdapter.findByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("[영속성 어댑터] 닉네임으로 회원 조회")
    void findByNickname() {
        // given
        String nickname = "nick";
        MemberJpaEntity entity = MemberJpaEntity.builder()
                .nickname(nickname)
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberJpaRepository.findByNickname(nickname)).willReturn(Optional.of(entity));

        // when
        Optional<Member> result = memberPersistenceAdapter.findByNickname(nickname);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("[영속성 어댑터] 회원 저장")
    void save() {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        MemberJpaEntity entity = MemberJpaEntity.builder()
                .id(1L)
                .email("test@test.com")
                .role(MemberRole.BUYER)
                .status(MemberStatus.ACTIVE)
                .build();
        given(memberJpaRepository.save(any(MemberJpaEntity.class))).willReturn(entity);

        // when
        Member result = memberPersistenceAdapter.save(member);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        verify(memberJpaRepository).save(any(MemberJpaEntity.class));
    }
}
