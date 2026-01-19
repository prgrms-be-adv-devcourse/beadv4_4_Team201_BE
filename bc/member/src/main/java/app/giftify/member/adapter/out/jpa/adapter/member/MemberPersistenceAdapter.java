package app.giftify.member.adapter.out.jpa.adapter.member;

import app.giftify.member.adapter.out.jpa.entity.member.MemberJpaEntity;
import app.giftify.member.adapter.out.jpa.mapper.member.MemberMapper;
import app.giftify.member.adapter.out.jpa.respository.member.MemberJpaRepository;
import app.giftify.member.application.port.out.member.MemberRepositoryPort;
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

    @Override
    public Optional<Member> findByNickname(String nickname) {
        return memberJpaRepository.findByNickname(nickname)
                .map(MemberMapper::toDomain);
    }
}
