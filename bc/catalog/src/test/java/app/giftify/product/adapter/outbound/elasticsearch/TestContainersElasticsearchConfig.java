package app.giftify.product.adapter.outbound.elasticsearch;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersElasticsearchConfig {

    private static final String ES_BASE_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:9.2.4";
    private static final DockerImageName ES_TEST_IMAGE;

    static {
        new ImageFromDockerfile("es-test:" + ES_BASE_IMAGE.split(":")[1], false)
                .withDockerfileFromBuilder(builder -> builder
                        .from(ES_BASE_IMAGE)
                        .run("elasticsearch-plugin install --batch analysis-nori analysis-icu")
                        .build())
                .get(); // 이미지 빌드 실행

        ES_TEST_IMAGE = DockerImageName.parse("es-test")
                .asCompatibleSubstituteFor(ES_BASE_IMAGE);
    }

    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer() {
        return new ElasticsearchContainer(ES_TEST_IMAGE)
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withEnv("xpack.security.enabled", "false")
                .withEnv("xpack.security.http.ssl.enabled", "false")
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }
}
