package app.giftify.shared.domain.event.member;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class SellerNicknameChangedEvent extends BaseDomainEvent {
    private final Long sellerId;
    private final String nickname;

    public SellerNicknameChangedEvent(Long sellerId, String nickname) {
        super();
        this.sellerId = sellerId;
        this.nickname = nickname;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return "SellerNicknameChangedEvent{" +
                "sellerId=" + sellerId +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
