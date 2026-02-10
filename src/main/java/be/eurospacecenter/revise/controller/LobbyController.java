package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.LobbyResponse;
import be.eurospacecenter.revise.exceptions.InvalidHostIdException;
import be.eurospacecenter.revise.service.LobbyService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping
    public LobbyResponse createLobby() {
        String lobbyCode = lobbyService.createLobby();
        return new LobbyResponse(lobbyCode);
    }

    @PostMapping("/{lobbyCode}/join")
    public void joinLobby(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestParam
            @Pattern(regexp = "^[a-zA-Z0-9\\s-_]+$", message = "Le label de l'équipe contient des caractères non autorisés")
            String teamLabel
    ) {
        try {
            lobbyService.joinLobby(lobbyCode, teamLabel);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Erreur pour rejoindre le lobby : " + e.getMessage());
        }
    }

    @PostMapping("/{lobbyCode}/start")
    public void startLobby(
            @PathVariable
            @Pattern(regexp = "^[A-Z]{6}$", message = "Code de lobby invalide")
            String lobbyCode,

            @RequestParam
            UUID hostId
    ) {
        try {
            lobbyService.startGame(lobbyCode, hostId);
        } catch (InvalidHostIdException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur pour démarrer le lobby : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Erreur pour démarrer le lobby : " + e.getMessage());
        }
    }
}