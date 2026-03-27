package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.MissionEndDTO;
import be.eurospacecenter.revise.dto.request.TeamMissionUpdateDTO;
import be.eurospacecenter.revise.dto.team.TeamFullProgressionDTO;
import be.eurospacecenter.revise.dto.team.TeamsProgressionDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.TeamFullProgression;
import be.eurospacecenter.revise.model.mission.TeamsProgression;
import be.eurospacecenter.revise.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
            @PathVariable String lobbyCode
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        TeamsProgression teamsProgression = missionService.getTeamsProgression(code);

        return TeamsProgressionDTO.fromTeamsProgression(teamsProgression);
    }

    @GetMapping(value = "/{lobbyCode}/team", params = "clientId")
    public TeamFullProgressionDTO getTeamFullProgression(
            @PathVariable String lobbyCode,

            @RequestParam @Valid
            UUID clientId
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        TeamFullProgression teamFullProgression = missionService.getTeamFullProgression(code, clientId);

        return TeamFullProgressionDTO.fromTeamFullProgression(teamFullProgression);
    }

    @PostMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMission(
            @PathVariable String lobbyCode,

            @RequestBody @Valid
            MissionEndDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        missionService.endMission(code, request.hostId());
    }

    @PutMapping("/{lobbyCode}")
    @Operation(description = "This endpoint allows both the host and the students to change the completion of a mission. **FOR THE HOST** `id=hostId` and `teamLabel` is the label of the team for which you want to change the mission completion. **FOR THE STUDENTS** `id=clientId` and `teamLabel` is ignored (can be empty or null).")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeTeamMissionsState(
            @PathVariable String lobbyCode,

            @RequestBody @Valid
            TeamMissionUpdateDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        missionService.changeTeamMissionsState(code, request.id(), request.teamLabel(), request.updateMissions());
    }
}