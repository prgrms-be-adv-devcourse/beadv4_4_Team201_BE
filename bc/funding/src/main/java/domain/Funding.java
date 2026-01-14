package domain;

import jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "FUNDING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funding extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_item_id", nullable = false)
    private WishlistItem wishlistItem;
    
    @OneToMany(mappedBy = "funding", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FundingMember> participants = new ArrayList<>();

    @Column(nullable = false)
    private Long targetAmount;
    
    @Column(nullable = false)
    private Long currentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FundingStatus status;

    @Column(nullable = false)
    private LocalDateTime endAt;

    // startAt은 BaseEntity에 createdAt으로 대체?

}