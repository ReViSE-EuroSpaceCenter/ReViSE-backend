package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.response.LobbyCreationResponse;
import be.eurospacecenter.revise.dto.response.LobbyJoinedResponse;
import be.eurospacecenter.revise.exceptions.InvalidStartLobbyException;
import be.eurospacecenter.revise.exceptions.NoAutoriseOperationException;
import be.eurospacecenter.revise.exceptions.NotFoundException;
import be.eurospacecenter.revise.helper.ResponseStatusHelper;
import be.eurospacecenter.revise.service.LobbyService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @RequestParam
            @Pattern(regexp = "[46]", message = "Le nombre d'équipes doit être 4 ou 6")
            String numberOfTeams
    ) {
        try {
            return lobbyService.createLobby(Integer.parseInt(numberOfTeams));
        } catch (IllegalArgumentException e) {
            throw ResponseStatusHelper.badRequest("Impossible de créer le lobby", e);
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

            @RequestParam
            UUID clientId,

            @RequestParam
            @Pattern(regexp = "^[A-Z]{4}$", message = "Label d'équipe invalide")
            String teamLabel
    ) {
        try {
            lobbyService.ensureClient(lobbyCode, clientId);
            lobbyService.assignTeam(lobbyCode, clientId, teamLabel);
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

            @RequestParam
            UUID hostId
    ) {
        try {
            lobbyService.ensureHost(lobbyCode, hostId);
            lobbyService.startGame(lobbyCode, hostId);
        } catch (NoAutoriseOperationException e){
            throw ResponseStatusHelper.forbidden("Action non autorisée", e);
        } catch (InvalidStartLobbyException e) {
            throw ResponseStatusHelper.badRequest("Impossible de démarrer la partie", e);
        }
    }
}
