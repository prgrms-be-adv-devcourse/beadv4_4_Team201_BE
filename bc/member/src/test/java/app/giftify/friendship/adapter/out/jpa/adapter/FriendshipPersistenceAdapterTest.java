package app.giftify.friendship.adapter.out.jpa.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.friendship.adapter.out.jpa.entity.FriendshipJpaEntity;
import app.giftify.friendship.adapter.out.jpa.repository.FriendshipJpaRepository;
import app.giftify.friendship.domain.Friendship;
import app.giftify.shared.domain.type.FriendshipStatus;

@ExtendWith(MockitoExtension.class)
class FriendshipPersistenceAdapterTest {

    @Mock
    private FriendshipJpaRepository repository;

    @InjectMocks
    private FriendshipPersistenceAdapter adapter;

    private static final Long FRIENDSHIP_ID = 1L;
    private static final Long REQUESTER_ID = 10L;
    private static final Long RECEIVER_ID = 20L;

    private FriendshipJpaEntity createEntity(FriendshipStatus status) {
        return new FriendshipJpaEntity(FRIENDSHIP_ID, REQUESTER_ID, RECEIVER_ID,
                status, status == FriendshipStatus.ACCEPTED ? LocalDateTime.now() : null);
    }

    private Friendship createDomain(FriendshipStatus status) {
        return new Friendship(FRIENDSHIP_ID, REQUESTER_ID, RECEIVER_ID,
                status, LocalDateTime.now(),
                status == FriendshipStatus.ACCEPTED ? LocalDateTime.now() : null);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Friendship 도메인을 Entity로 변환하여 저장 후 도메인 반환")
        void save_ConvertsAndReturns() {
            // given
            Friendship domain = createDomain(FriendshipStatus.PENDING);
            FriendshipJpaEntity savedEntity = createEntity(FriendshipStatus.PENDING);
            given(repository.save(any(FriendshipJpaEntity.class))).willReturn(savedEntity);

            // when
            Friendship result = adapter.save(domain);

            // then
            assertThat(result.getId()).isEqualTo(FRIENDSHIP_ID);
            assertThat(result.getRequesterId()).isEqualTo(REQUESTER_ID);
            assertThat(result.getReceiverId()).isEqualTo(RECEIVER_ID);
            assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);
            then(repository).should().save(any(FriendshipJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 도메인 반환")
        void found_ReturnsDomain() {
            // given
            given(repository.findById(FRIENDSHIP_ID))
                    .willReturn(Optional.of(createEntity(FriendshipStatus.PENDING)));

            // when
            Optional<Friendship> result = adapter.findById(FRIENDSHIP_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(FRIENDSHIP_ID);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void notFound_ReturnsEmpty() {
            // given
            given(repository.findById(999L)).willReturn(Optional.empty());

            // when
            Optional<Friendship> result = adapter.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByMemberPairAndStatusIn")
    class ExistsByMemberPairAndStatusIn {

        @Test
        @DisplayName("JpaRepository에 위임 호출 검증")
        void delegatesToRepository() {
            // given
            List<FriendshipStatus> statuses = List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED);
            given(repository.existsByMemberPairAndStatusIn(REQUESTER_ID, RECEIVER_ID, statuses))
                    .willReturn(true);

            // when
            boolean result = adapter.existsByMemberPairAndStatusIn(REQUESTER_ID, RECEIVER_ID, statuses);

            // then
            assertThat(result).isTrue();
            then(repository).should().existsByMemberPairAndStatusIn(REQUESTER_ID, RECEIVER_ID, statuses);
        }
    }

    @Nested
    @DisplayName("findAllByMemberIdAndStatus")
    class FindAllByMemberIdAndStatus {

        @Test
        @DisplayName("Entity 리스트를 도메인 리스트로 변환하여 반환")
        void returnsMappedDomainList() {
            // given
            List<FriendshipJpaEntity> entities = List.of(
                    createEntity(FriendshipStatus.ACCEPTED),
                    createEntity(FriendshipStatus.ACCEPTED)
            );
            given(repository.findAllByMemberIdAndStatus(REQUESTER_ID, FriendshipStatus.ACCEPTED))
                    .willReturn(entities);

            // when
            List<Friendship> result = adapter.findAllByMemberIdAndStatus(REQUESTER_ID, FriendshipStatus.ACCEPTED);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        }
    }

    @Nested
    @DisplayName("findAllByReceiverIdAndStatus")
    class FindAllByReceiverIdAndStatus {

        @Test
        @DisplayName("수신자 기준으로 조회 후 도메인 리스트 반환")
        void returnsMappedDomainList() {
            // given
            given(repository.findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
                    RECEIVER_ID, FriendshipStatus.PENDING))
                    .willReturn(List.of(createEntity(FriendshipStatus.PENDING)));

            // when
            List<Friendship> result = adapter.findAllByReceiverIdAndStatus(RECEIVER_ID, FriendshipStatus.PENDING);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReceiverId()).isEqualTo(RECEIVER_ID);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deleteById 호출 검증")
        void callsDeleteById() {
            // given
            Friendship domain = createDomain(FriendshipStatus.ACCEPTED);

            // when
            adapter.delete(domain);

            // then
            then(repository).should().deleteById(FRIENDSHIP_ID);
        }
    }
}
