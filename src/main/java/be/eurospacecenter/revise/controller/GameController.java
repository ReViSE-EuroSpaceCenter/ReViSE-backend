package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.TeamMissionStatusUpdateRequest;
import be.eurospacecenter.revise.exceptions.InvalidGameOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
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
            gameService.changeTeamMissionState(lobbyCode, request.clientId(), request.missionNumber());
        } catch (InvalidGameOperationException | NotFoundException e) {
            throw ResponseStatusHelper.badRequest("Erreur pour terminer la mission", e);
        }
    }
}