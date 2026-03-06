package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.TeamMissionStatusUpdateRequest;
import be.eurospacecenter.revise.dto.response.TeamFullProgressionResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.service.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@Validated
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{lobbyCode}")
    public TeamsProgressionResponse getGameInfo(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode
    ) {
        return gameService.getTeamsProgression(lobbyCode);
    }

    @GetMapping("/{lobbyCode}/{clientId}/missions")
    public TeamFullProgressionResponse getTeamMissions(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @PathVariable
            @Valid
            UUID clientId
    ) {
        return gameService.getTeamFullProgression(lobbyCode, clientId);
    }

    @PutMapping("/{lobbyCode}/missions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeTeamMissionState(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            TeamMissionStatusUpdateRequest request
    ) {
        gameService.changeTeamMissionState(lobbyCode, request.clientId(), request.updateMissions());
    }
}