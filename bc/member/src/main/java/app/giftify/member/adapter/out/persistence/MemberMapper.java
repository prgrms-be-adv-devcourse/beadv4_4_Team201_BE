package app.giftify.member.adapter.out.persistence;

import app.giftify.member.core.domain.member.Member;

// 도메인 모델(Member)과 JPA 엔티티(MemberJpaEntity) 간의 변환 담당
public class MemberMapper {

    public static Member toDomain(MemberJpaEntity entity) {
        if (entity == null) return null;

        return Member.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getModifiedAt())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .nickname(entity.getNickname())
                .birthday(entity.getBirthday())
                .role(entity.getRole())
                .address(entity.getAddress())
                .phoneNum(entity.getPhoneNum())
                .name(entity.getName())
                .status(entity.getStatus())
                .authSub(entity.getAuthSub())
                .build();
    }

    public static MemberJpaEntity toEntity(Member domain) {
        if (domain == null) return null;

        return MemberJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .nickname(domain.getNickname())
                .birthday(domain.getBirthday())
                .role(domain.getRole())
                .address(domain.getAddress())
                .phoneNum(domain.getPhoneNum())
                .name(domain.getName())
                .status(domain.getStatus())
                .authSub(domain.getAuthSub())
                .build();
    }
}
