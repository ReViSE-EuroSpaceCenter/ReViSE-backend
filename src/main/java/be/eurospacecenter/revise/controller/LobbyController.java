package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.AssignTeamRequest;
import be.eurospacecenter.revise.dto.request.CreateLobbyRequest;
import be.eurospacecenter.revise.dto.request.StartLobbyRequest;
import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyInfoResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.helper.ResponseStatusHelper;
import be.eurospacecenter.revise.service.LobbyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
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
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode
    ) {
        try {
            return lobbyService.getLobbyInfo(lobbyCode);
        } catch (NotFoundException e) {
            throw ResponseStatusHelper.notFound("Impossible de récupérer les informations du lobby", e);
        }
    }

    @PostMapping("/{lobbyCode}/join")
    public LobbyJoinedResponse joinLobby(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode
    ) {
        try {
            return lobbyService.joinLobby(lobbyCode);
        } catch (NotFoundException e) {
            throw ResponseStatusHelper.notFound("Lobby introuvable", e);
        }
    }

    @PostMapping("/{lobbyCode}/team")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignTeam(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestBody @Valid
            AssignTeamRequest request
    ) {
        try {
            lobbyService.assignTeam(
                    lobbyCode,
                    request.clientId(),
                    request.teamLabel()
            );
        } catch (NoAutoriseOperationException e) {
            throw ResponseStatusHelper.forbidden("Action non autorisée", e);
        } catch (IllegalArgumentException e) {
            throw ResponseStatusHelper.badRequest("Impossible d'assigner l'équipe", e);
        }
    }

    @PostMapping("/{lobbyCode}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startLobby(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestBody @Valid
            StartLobbyRequest request
    ) {
        try {
            lobbyService.startGame(lobbyCode, request.hostId());
        } catch (NoAutoriseOperationException e) {
            throw ResponseStatusHelper.forbidden("Action non autorisée", e);
        } catch (InvalidStartLobbyException e) {
            throw ResponseStatusHelper.badRequest("Impossible de démarrer la partie", e);
        }
    }

}
