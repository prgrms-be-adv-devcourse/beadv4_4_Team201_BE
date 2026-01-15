package app.giftify.member.adapter.out.persistence;

import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.core.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberPersistenceAdapter implements MemberRepositoryPort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findByAuthSub(String authSub) {
        return memberJpaRepository.findByAuthSub(authSub)
                .map(MemberMapper::toDomain);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id)
                .map(MemberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .map(MemberMapper::toDomain);
    }

    @Override
    public Member save(Member member) {
        MemberJpaEntity entity = MemberMapper.toEntity(member);
        MemberJpaEntity savedEntity = memberJpaRepository.save(entity);
        return MemberMapper.toDomain(savedEntity);
    }
}
