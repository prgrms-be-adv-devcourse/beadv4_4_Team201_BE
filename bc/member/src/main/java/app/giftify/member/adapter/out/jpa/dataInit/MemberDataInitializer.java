package app.giftify.member.adapter.out.jpa.dataInit;

import app.giftify.member.adapter.out.jpa.entity.MemberJpaEntity;
import app.giftify.member.adapter.out.jpa.respository.MemberJpaRepository;
import app.giftify.member.domain.member.MemberStatus;
import app.giftify.shared.domain.type.MemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDataInitializer implements ApplicationRunner {

	private final MemberJpaRepository memberJpaRepository;

	@Override
	public void run(ApplicationArguments args) {
		log.info("===== MemberDataInitializer 시작 =====");

		MemberJpaEntity member1 = MemberJpaEntity.builder()
			.email("dev4.team201@gmail.com")
			.password("dev1234!!")
			.nickname("dev4.team201")
			.birthday(LocalDate.of(1970, 11, 8))
			.role(MemberRole.BUYER)
			.address("서울시 송파구")
			.phoneNum("010-1234-5678")
			.name("Team201 DEV4")
			.status(MemberStatus.ACTIVE)
			.authSub("google-oauth2|104844495450678108304")
			.build();
		MemberJpaEntity member2 = MemberJpaEntity.builder()
			.email("qa-giftify-test-mock@giftify.app")
			.password("@5675995dc43786719eb76Cd455d3b368")
			.nickname("나른한고양이0013")
			.birthday(LocalDate.of(1970, 11, 8))
			.role(MemberRole.SELLER)
			.address("서울시 송파구")
			.phoneNum("010-1234-5678")
			.name("김영주")
			.status(MemberStatus.ACTIVE)
			.authSub("auth0|697c550a745dc4abd34fea91")
			.build();

		saveIfNotExists(member1);
		saveIfNotExists(member2);

		log.info("===== MemberDataInitializer 완료 =====");
	}

	private void saveIfNotExists(MemberJpaEntity member) {
		if (memberJpaRepository.findByAuthSub(member.getAuthSub()).isEmpty()) {
			memberJpaRepository.save(member);
			log.info("Member 추가: email={}, authSub={}", member.getEmail(), member.getAuthSub());
		} else {
			log.info("Member 이미 존재 (스킵): email={}, authSub={}", member.getEmail(), member.getAuthSub());
		}
	}
}
