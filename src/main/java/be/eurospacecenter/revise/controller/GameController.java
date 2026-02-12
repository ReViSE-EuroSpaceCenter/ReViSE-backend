package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.ScoreResponse;
import be.eurospacecenter.revise.service.GameService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{lobbyCode}/score")
    public ScoreResponse calculateScore(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode
    ) {
        int score = gameService.getGeneralScore(lobbyCode);
        return new ScoreResponse(score);
    }
}