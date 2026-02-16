package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.CompleteTeamMissionRequest;
import be.eurospacecenter.revise.dto.response.ScoreResponse;
import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.helper.ResponseStatusHelper;
import be.eurospacecenter.revise.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/{lobbyCode}/complete")
    public void completeATeamMission(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestBody @Valid
            CompleteTeamMissionRequest request
    ) {
        try {
            gameService.completeATeamMission(lobbyCode, request.clientId(), request.missionNumber(), request.resources());
        } catch (InvalidGameOperationException e) {
            throw ResponseStatusHelper.badRequest("Erreur pour terminer la mission", e);
        }
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
            throw ResponseStatusHelper.badRequest("Erreur pour récupérer le score", e);
        }
    }
}