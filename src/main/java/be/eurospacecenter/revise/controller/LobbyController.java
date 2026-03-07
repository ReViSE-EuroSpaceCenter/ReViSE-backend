package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.AssignTeamRequest;
import be.eurospacecenter.revise.dto.request.CreateLobbyRequest;
import be.eurospacecenter.revise.dto.request.StartLobbyRequest;
import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyCreationResponse createLobby(
            @RequestBody @Valid
            CreateLobbyRequest request
    ) {
        return lobbyService.createLobby(request.numberOfTeams());
    }

    @GetMapping("/{lobbyCode}")
    public LobbyInfoResponse getLobbyInfo(
            @PathVariable
            @Pattern(regexp = LobbyCode.PATTERN, message = "Code de lobby invalide")
            String lobbyCode
    ) {
        return lobbyService.getLobbyInfo(lobbyCode);
    }

    @PostMapping("/{lobbyCode}/join")
    public LobbyJoinedResponse joinLobby(
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
            AssignTeamRequest request
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
            StartLobbyRequest request
    ) {
        lobbyService.startGame(lobbyCode, request.hostId());
    }

}
