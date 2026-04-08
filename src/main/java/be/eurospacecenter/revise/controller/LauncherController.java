package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.HostIdDTO;
import be.eurospacecenter.revise.dto.team.TeamsProgressionDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.TeamsProgression;
import be.eurospacecenter.revise.service.LauncherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/launcher")
public class LauncherController {
    private final LauncherService launcherService;

    public LauncherController(LauncherService launcherService) {
        this.launcherService = launcherService;
    }

    @GetMapping("/{lobbyCode}")
    public TeamsProgressionDTO getTeamsFullProgression(
            @PathVariable String lobbyCode
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        TeamsProgression teamsProgression = launcherService.getTeamsProgression(code);

        return TeamsProgressionDTO.fromTeamsProgression(teamsProgression);
    }

    @PostMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endLauncher(
            @PathVariable
            String lobbyCode,

            @RequestBody
            HostIdDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        launcherService.endLauncher(code, request.hostId());
    }

    @PostMapping("/{lobbyCode}/gameOver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMission(
            @PathVariable String lobbyCode,

            @RequestBody @Valid
            HostIdDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        launcherService.endGame(code, request.hostId());
    }
}
