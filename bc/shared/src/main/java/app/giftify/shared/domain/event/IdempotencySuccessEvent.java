package app.giftify.shared.domain.event;

public class IdempotencySuccessEvent extends BaseDomainEvent {
    private final String idempotencyKey;
    private final String payloadHash;
    private final String domainType;
    private final Long requesterId;

    public IdempotencySuccessEvent(String idempotencyKey, String payloadHash, String domainType, Long requesterId) {
        this.idempotencyKey = idempotencyKey;
        this.payloadHash = payloadHash;
        this.domainType = domainType;
        this.requesterId = requesterId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public String getDomainType() {
        return domainType;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    @Override
    public String toString() {
        return "IdempotencySuccessEvent{" +
                "idempotencyKey='" + idempotencyKey + '\'' +
                ", payloadHash='" + payloadHash + '\'' +
                ", domainType='" + domainType + '\'' +
                ", requesterId=" + requesterId +
                '}';
    }
}
