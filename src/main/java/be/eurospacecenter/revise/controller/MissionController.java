package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.EndMissionRequest;
import be.eurospacecenter.revise.dto.request.TeamMissionStatusUpdateRequest;
import be.eurospacecenter.revise.dto.response.TeamFullProgressionResponse;
import be.eurospacecenter.revise.dto.response.TeamsProgressionResponse;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.helper.LobbyCode;
import be.eurospacecenter.revise.service.MissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@Validated
@RequestMapping("/api/missions")
public class MissionController {
    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping("/{lobbyCode}")
    public TeamsProgressionResponse getGameInfo(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode
    ) {
        return missionService.getTeamsProgression(lobbyCode);
    }

    @PutMapping("/{lobbyCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeTeamMissionState(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            TeamMissionStatusUpdateRequest request
    ) {
        missionService.changeTeamMissionState(lobbyCode, request.clientId(), request.missionNumber());
    }

    @PostMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMission(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            EndMissionRequest request
    ) {
        missionService.endMission(lobbyCode, request.hostId());
    }

    @GetMapping("/{lobbyCode}/{clientId}")
    public TeamFullProgressionResponse getTeamMissions(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @PathVariable
            @Valid
            UUID clientId
    ) {
        return missionService.getTeamFullProgression(lobbyCode, clientId);
    }
}