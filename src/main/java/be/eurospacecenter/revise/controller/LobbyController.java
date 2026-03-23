package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.AssignTeamDTO;
import be.eurospacecenter.revise.dto.request.CreateLobbyDTO;
import be.eurospacecenter.revise.dto.request.StartLobbyDTO;
import be.eurospacecenter.revise.dto.response.LobbyCreationDTO;
import be.eurospacecenter.revise.dto.response.LobbyInfoDTO;
import be.eurospacecenter.revise.dto.response.LobbyJoinedDTO;
import be.eurospacecenter.revise.exceptions.ErrorKeys;
import be.eurospacecenter.revise.helper.LobbyCode;
import be.eurospacecenter.revise.service.LobbyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = "Code de lobby invalide")
            String lobbyCode
    ) {
        return lobbyService.getLobbyInfo(lobbyCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyCreationDTO createLobby(
            @RequestBody @Valid
            CreateLobbyDTO request
    ) {
        return lobbyService.createLobby(request.numberOfTeams());
    }

    @PostMapping("/{lobbyCode}/join")
    public LobbyJoinedDTO joinLobby(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode
    ) {
        return lobbyService.joinLobby(lobbyCode);
    }

    @PostMapping("/{lobbyCode}/team")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignTeam(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            AssignTeamDTO request
    ) {
        lobbyService.assignTeam(
                lobbyCode,
                request.clientId(),
                request.teamLabel()
        );
    }

    @PostMapping("/{lobbyCode}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startLobby(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = ErrorKeys.INVALID_LOBBY_CODE)
            String lobbyCode,

            @RequestBody @Valid
            StartLobbyDTO request
    ) {
        lobbyService.startGame(lobbyCode, request.hostId());
    }

}
