package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.UpdateResourceRequest;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.helper.LobbyCode;
import be.eurospacecenter.revise.service.LauncherService;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/launchers")
public class LauncherController {

    private final LauncherService launcherService;

    public LauncherController(LauncherService launcherService) {
        this.launcherService = launcherService;
    }

    @PostMapping("/{lobbyCode}")
    public void updateResources(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody
            UpdateResourceRequest request
    ) {
        launcherService.updateResources(lobbyCode, request.clientId(), request.resourceName());
    }
}
