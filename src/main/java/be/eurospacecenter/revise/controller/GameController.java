package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.ScoreResponse;
import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.service.GameService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


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
        try {
            int score = gameService.getGeneralScore(lobbyCode);
            return new ScoreResponse(score);
        } catch (InvalidGameOperationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur pour récupérer le score : " + e.getMessage());
        }
    }
}