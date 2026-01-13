package domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FUNDING_MEMBER")
public class FundingMember { //todo extends ReplicaMember
	@Id
	private Long id;

	public FundingMember() {
	}

	public FundingMember(Long id) {
		this.id = id;
	}
}
