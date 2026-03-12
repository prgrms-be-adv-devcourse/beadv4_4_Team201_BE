package app.giftify.product.adapter.outbound.elasticsearch;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersOllamaConfig {

    private static final String OLLAMA_IMAGE = "ollama/ollama:latest";

    @Bean
    @ServiceConnection
    OllamaContainer ollamaContainer() throws IOException, InterruptedException {
        OllamaContainer container = new OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE))
                .withStartupTimeout(Duration.ofMinutes(3))
                .withReuse(true);

        container.start();
        container.execInContainer("ollama", "pull", "bge-m3");

        return container;
    }
}
