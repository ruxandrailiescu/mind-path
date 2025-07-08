package ro.ase.acs.mind_path.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key}")
    private String apiKey;
    private Duration timeout = Duration.ofSeconds(15);

    @Bean
    public OpenAIClient openAi() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key is missing.");
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .timeout(timeout)
                .build();
    }
}
