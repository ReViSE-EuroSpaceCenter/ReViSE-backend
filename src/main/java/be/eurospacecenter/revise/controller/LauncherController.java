package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.ResourceUpdateDTO;
import be.eurospacecenter.revise.dto.response.ScoreDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.helper.LobbyCode;
import be.eurospacecenter.revise.service.LauncherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestParam @Valid
            UUID hostId
    ) {
        return launcherService.getGeneralScore(lobbyCode, hostId);
    }

    @PutMapping("/{lobbyCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResources(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody
            ResourceUpdateDTO request
    ) {
        launcherService.updateResources(lobbyCode, request.clientId(), request.resources());
    }
}
