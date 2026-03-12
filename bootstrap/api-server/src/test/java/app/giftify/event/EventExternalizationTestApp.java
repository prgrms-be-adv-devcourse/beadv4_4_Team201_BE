package app.giftify.event;

import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = {
                SharedSecurityAutoConfiguration.class
        },
        excludeName = {
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
                "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
                "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration",
                "org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration",
                "org.springframework.modulith.runtime.autoconfigure.SpringModulithRuntimeAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration",
                "org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration",
                "org.springframework.ai.model.ollama.autoconfigure.OllamaApiAutoConfiguration",
                "org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration",
                "org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration"
        }
)
class EventExternalizationTestApp {
}
