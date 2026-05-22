package app.giftify.wishlist.readmodel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberViewRepository extends JpaRepository<MemberView, Long> {
    List<MemberView> findByNicknameContainingIgnoreCase(String nickname);
}
