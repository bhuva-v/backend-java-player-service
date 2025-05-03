package com.app.playerservicejava.controller.chat;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.service.PlayerService;
import com.app.playerservicejava.service.chat.ChatClientService;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "v1/chat", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatClientService chatClientService;

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public @ResponseBody String chat() throws OllamaBaseException, IOException, InterruptedException {
        return chatClientService.chat();
    }

    @GetMapping("/list-models")
    public ResponseEntity<List<Model>> listModels() throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        List<Model> models = chatClientService.listModels();
        return ResponseEntity.ok(models);
    }

    @PostMapping("/generate-nickname")
    public ResponseEntity<?> generateNickname(@RequestParam String playerId) throws OllamaBaseException, IOException, InterruptedException {
        if (playerId == null || playerId.trim().isEmpty()) {
            LOGGER.error("generateNickname: playerId is null or empty");
            return ResponseEntity.badRequest().body("Player ID is required");
        }

        Optional<Player> playerOpt = playerService.getPlayerById(playerId);

        if (!playerOpt.isPresent()) {
            LOGGER.error("generateNickname: Player with ID {} not found", playerId);
            return ResponseEntity.badRequest().body("Player with ID " + playerId + " not found.");
        }

        Player player = playerOpt.get();
        String birthCountry = player.getBirthCountry();

        String systemPrompt = "You are a creative nickname generator for " + birthCountry + " baseball players. Generate a unique, cool, and memorable nickname based on the player's information.";
        String userPrompt = "Generate a nickname for player ID " + playerId + ". "
                + "Name: " + player.getFirstName() + " " + player.getLastName() + ". "
                + "Birth Year: " + player.getBirthYear() + ". "
                + "Position: " + (player.getBats().equals(player.getThrowStats()) ? "Two-way player" : (player.getBats().equals("R") ? "Right-handed batter" : "Left-handed batter")) + ". "
                + "Please provide only the nickname without any additional text or explanation.";

        String nickname;
        try {
            nickname = chatClientService.chat(systemPrompt, userPrompt);
        } catch (Exception e) {
            LOGGER.error("generateNickname: Error generating nickname for player {}", playerId, e);
            return ResponseEntity.internalServerError().body("Error generating nickname: " + e.getMessage());
        }

        LOGGER.info("generateNickname: Nickname generated for player {}: {}", playerId, nickname);
        return ResponseEntity.ok()
                .body("{\"playerId\":\"" + playerId + "\",\"nickname\":\"" + nickname.trim() + "\"}");
    }
}
