package ro.ase.acs.mind_path.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGradingService {

    public record GradeResult(float score, String feedback) {}

    private final OpenAIClient openAi;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AiGradingService.class);

    public GradeResult grade(String question, String rubric, String answer) throws JsonProcessingException {
        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addSystemMessage("""
                You are a strict grader.
                Given the rubric and the student's answer,
                respond ONLY with valid JSON: {"score": float 0-1, "feedback": string}
                """)
                .addUserMessage("""
                QUESTION:
                %s

                RUBRIC:
                %s

                STUDENT:
                %s
                """.formatted(question, rubric, answer))
                .responseFormat(ChatCompletionCreateParams.ResponseFormat.ofJsonObject(
                        ResponseFormatJsonObject.builder().build()))
                .temperature(0.0)
                .build();

        ChatCompletion res = openAi.chat().completions().create(createParams);
        ChatCompletionMessage msg = res.choices().getFirst().message();
        String json = msg.content()
                .orElseThrow(() -> new IllegalStateException("OpenAI returned no content"));
        logger.info("raw content OpenAI = {}", json);

        float score = (float) objectMapper.readTree(json).get("score").asDouble();
        String feedback = objectMapper.readTree(json).get("feedback").asText();

        return new GradeResult(score, feedback);
    }
}
