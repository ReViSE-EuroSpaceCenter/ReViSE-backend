package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.ResourceUpdateDTO;
import be.eurospacecenter.revise.dto.response.ScoreDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.service.LauncherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/launchers")
public class LauncherController {

    private final LauncherService launcherService;

    public LauncherController(LauncherService launcherService) {
        this.launcherService = launcherService;
    }

    @GetMapping(value = "/{lobbyCode}/score", params = "hostId")
    public ScoreDTO getGeneralScore(
            @PathVariable
            String lobbyCode,

            @RequestParam @Valid
            UUID hostId
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        return launcherService.getGeneralScore(code, hostId);
    }

    @PutMapping("/{lobbyCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResources(
            @PathVariable
            String lobbyCode,

            @RequestBody
            ResourceUpdateDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        launcherService.updateResources(code, request.clientId(), request.resources());
    }
}
