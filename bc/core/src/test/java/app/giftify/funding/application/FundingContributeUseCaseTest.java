package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingContributeRequest;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.replica.MemberReplica;
import app.giftify.replica.MemberReplicaRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingContributeUseCaseTest {

    @InjectMocks
    private FundingContributeUseCase fundingContributeUseCase;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingParticipantMemberRepository fundingParticipantMemberRepository;

    @Mock
    private MemberReplicaRepository memberReplicaRepository;

    @Mock
    private EventPublisher eventPublisher;

    private MemberReplica participant;
    private String nickname = "testUser";

    @BeforeEach
    void setUp() {
        participant = mock(MemberReplica.class);
        // note: participant.getNickname()은 필요한 테스트에서만 given을 설정하도록 변경
    }

    @Test
    @DisplayName("contribute 성공: 새로운 참여자가 펀딩에 기여한다.")
    void contribute_Success_NewParticipant() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;
        FundingContributeRequest request = new FundingContributeRequest(fundingId, amount);
        List<FundingContributeRequest> requests = List.of(request);

        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(fundingRepository.findAllById(anyList())).willReturn(List.of(funding));
        
        given(participant.getNickname()).willReturn(nickname);
        given(memberReplicaRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(null);

        // when
        List<Funding> result = fundingContributeUseCase.contribute(requests, participantId);

        // then
        assertThat(result).containsExactly(funding);
        verify(funding).contribute(amount);

        ArgumentCaptor<FundingParticipantMember> captor = ArgumentCaptor.forClass(FundingParticipantMember.class);
        verify(fundingParticipantMemberRepository).save(captor.capture());
        FundingParticipantMember savedMember = captor.getValue();

        assertThat(savedMember.getNickName()).isEqualTo(nickname);
        assertThat(savedMember.getAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("contribute 성공: 기존 참여자가 펀딩에 추가로 기여한다.")
    void contribute_Success_ExistingParticipant() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;
        FundingContributeRequest request = new FundingContributeRequest(fundingId, amount);
        List<FundingContributeRequest> requests = List.of(request);

        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        FundingParticipantMember member = mock(FundingParticipantMember.class);

        given(fundingRepository.findAllById(anyList())).willReturn(List.of(funding));
        given(memberReplicaRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(member);

        // when
        List<Funding> result = fundingContributeUseCase.contribute(requests, participantId);

        // then
        assertThat(result).containsExactly(funding);
        verify(funding).contribute(amount);
        verify(member).addAmount(amount);
        verify(fundingParticipantMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("contribute 성공: 펀딩 목표 달성 시 이벤트를 발행한다.")
    void contribute_Success_FundingAchieved() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;
        FundingContributeRequest request = new FundingContributeRequest(fundingId, amount);
        List<FundingContributeRequest> requests = List.of(request);

        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(funding.getWishlistItemId()).willReturn(10L);
        given(funding.contribute(amount)).willReturn(true);

        given(fundingRepository.findAllById(anyList())).willReturn(List.of(funding));
        given(participant.getNickname()).willReturn(nickname);
        given(memberReplicaRepository.findById(participantId)).willReturn(Optional.of(participant));
        given(fundingParticipantMemberRepository.findByFundingAndParticipantId(funding, participantId)).willReturn(null);

        // when
        fundingContributeUseCase.contribute(requests, participantId);

        // then
        verify(eventPublisher).publish(any(FundingAchievedEvent.class));
    }

    @Test
    @DisplayName("contribute 실패: 요청에 해당하는 펀딩을 찾을 수 없다.")
    void contribute_Fail_FundingNotFound() {
        // given
        Long fundingId = 1L;
        Long participantId = 2L;
        Integer amount = 10000;
        FundingContributeRequest request = new FundingContributeRequest(fundingId, amount);
        List<FundingContributeRequest> requests = List.of(request);

        given(fundingRepository.findAllById(anyList())).willReturn(Collections.emptyList());
        // memberReplicaRepository.findById()는 예외 발생 전에 호출되므로 모킹이 필요합니다.
        given(memberReplicaRepository.findById(participantId)).willReturn(Optional.of(participant));

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(requests, participantId))
                .isInstanceOf(FundingException.class)
                .hasFieldOrPropertyWithValue("errorCode", FundingErrorCode.FUNDING_NOT_FOUND);
    }
}
