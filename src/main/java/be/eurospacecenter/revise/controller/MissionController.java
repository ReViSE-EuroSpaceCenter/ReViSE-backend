package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.EndMissionDTO;
import be.eurospacecenter.revise.dto.request.TeamMissionUpdateDTO;
import be.eurospacecenter.revise.dto.response.TeamFullProgressionDTO;
import be.eurospacecenter.revise.dto.response.TeamsProgressionDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.helper.LobbyCode;
import be.eurospacecenter.revise.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
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
    public TeamsProgressionDTO getTeamsFullProgression(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode
    ) {
        return missionService.getTeamsFullProgression(lobbyCode);
    }

    @GetMapping(value = "/{lobbyCode}/team", params = "clientId")
    public TeamFullProgressionDTO getTeamFullProgression(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestParam @Valid
            UUID clientId
    ) {
        return missionService.getTeamFullProgression(lobbyCode, clientId);
    }

    @PostMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMission(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            EndMissionDTO request
    ) {
        missionService.endMission(lobbyCode, request.hostId());
    }

    @PutMapping("/{lobbyCode}")
    @Operation(description = "This endpoint allows both the host and the students to change the completion of a mission. **FOR THE HOST** `id=hostId` and `teamLabel` is the label of the team for which you want to change the mission completion. **FOR THE STUDENTS** `id=clientId` and `teamLabel` is ignored (can be empty or null).")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeTeamMissionsState(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            TeamMissionUpdateDTO request
    ) {
        missionService.changeTeamMissionsState(lobbyCode, request.id(), request.teamLabel(), request.updateMissions());
    }
}