package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.TeamMissionStatusUpdateRequest;
import be.eurospacecenter.revise.dto.response.ScoreResponse;
import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.helper.ResponseStatusHelper;
import be.eurospacecenter.revise.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PutMapping("/{lobbyCode}/missions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeATeamMission(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestBody @Valid
            TeamMissionStatusUpdateRequest request
    ) {
        try {
            gameService.changeATeamMissionState(lobbyCode, request.clientId(), request.missionNumber());
        } catch (InvalidGameOperationException e) {
            throw ResponseStatusHelper.badRequest("Erreur pour terminer la mission", e);
        }
    }

    @GetMapping("/{lobbyCode}/score")
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