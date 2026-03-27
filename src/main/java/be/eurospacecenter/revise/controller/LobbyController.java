package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.AssignTeamDTO;
import be.eurospacecenter.revise.dto.request.CreateLobbyDTO;
import be.eurospacecenter.revise.dto.request.StartLobbyDTO;
import be.eurospacecenter.revise.dto.response.LobbyCreationDTO;
import be.eurospacecenter.revise.dto.response.LobbyInfoDTO;
import be.eurospacecenter.revise.dto.response.LobbyJoinedDTO;
import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.model.lobby.LobbyCreation;
import be.eurospacecenter.revise.model.lobby.LobbyJoined;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.service.LobbyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/lobbies")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/{lobbyCode}")
    public LobbyInfoDTO getLobbyInfo(
            @PathVariable String lobbyCode
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        Lobby lobby = lobbyService.getLobbyInfo(code);

        return LobbyInfoDTO.fromLobby(lobby);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyCreationDTO createLobby(
            @RequestBody @Valid CreateLobbyDTO request
    ) {
        LobbyCreation lobbyCreation = lobbyService.createLobby(request.numberOfTeams());
        return LobbyCreationDTO.fromLobbyCreation(lobbyCreation);
    }

    @PostMapping("/{lobbyCode}/join")
    public LobbyJoinedDTO joinLobby(
            @PathVariable String lobbyCode
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        LobbyJoined lobbyJoined = lobbyService.joinLobby(code);

        return LobbyJoinedDTO.fromLobbyJoined(lobbyJoined);
    }

    @PostMapping("/{lobbyCode}/team")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignTeam(
            @PathVariable String lobbyCode,

            @RequestBody @Valid AssignTeamDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        lobbyService.assignTeam(code, request.clientId(), request.teamLabel());
    }

    @PostMapping("/{lobbyCode}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startLobby(
            @PathVariable String lobbyCode,

            @RequestBody @Valid StartLobbyDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        lobbyService.startGame(code, request.hostId());
    }

}
