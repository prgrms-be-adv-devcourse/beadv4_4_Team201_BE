package app.giftify.catalogmember.adapter.outbound.jpa;

import app.giftify.catalogmember.core.domain.CatalogMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogMemberRepository extends JpaRepository<CatalogMember, Long> {
}
