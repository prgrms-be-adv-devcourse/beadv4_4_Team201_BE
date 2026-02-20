package app.giftify.settlement.adapter.outbound.batch.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "settlement.batch")
public record BatchProperties(
        String validationCutOffTime,
        int chunkSize,
        int retryLimit,
        int orderPerPartition
) {
}
