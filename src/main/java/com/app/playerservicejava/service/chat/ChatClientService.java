package com.app.playerservicejava.service.chat;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.Model;
import io.github.ollama4j.models.OllamaResult;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatRequestModel;
import io.github.ollama4j.types.OllamaModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.PromptBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class ChatClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientService.class);

    @Autowired
    private OllamaAPI ollamaAPI;

    public List<Model> listModels() throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        List<Model> models = ollamaAPI.listModels();
        return models;
    }

    public String chat() throws OllamaBaseException, IOException, InterruptedException {
        try {
            String model = OllamaModelType.TINYLLAMA;

            // https://ollama4j.github.io/ollama4j/intro
            PromptBuilder promptBuilder =
                    new PromptBuilder()
                            .addLine("Recite a haiku about recursion.");

            boolean raw = false;
            OllamaResult response = ollamaAPI.generate(model, promptBuilder.build(), raw, new OptionsBuilder().build());
            return response.getResponse();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String chat(String systemPrompt, String userPrompt) throws OllamaBaseException, IOException, InterruptedException {

        String model = "llama3.2:1b";
        // https://ollama4j.github.io/ollama4j/intro
        OllamaChatRequestModel chatRequest = OllamaChatRequestBuilder.getInstance(model)
                .withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt)
                .withMessage(OllamaChatMessageRole.USER, userPrompt)
                .build();

        OllamaResult response = ollamaAPI.chat(chatRequest);
        return  response.getResponse();
    }
}
