package domain;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUNDING_MEMBER")
@Getter
@NoArgsConstructor
public class FundingMember { //todo extends ReplicaMember
	@Id
	private Long id;

	private String email;
	private String nickname;
	private Date birthday;
	private String address;
	private String phoneNumber;
	private String name;

	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public FundingMember(Long id) {
		this.id = id;
	}

	public FundingMember(Long id, String email, String nickname, Date birthday, String address, String phoneNumber,
		String name, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.birthday = birthday;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.name = name;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}
}
