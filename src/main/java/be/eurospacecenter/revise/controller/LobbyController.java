package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.service.LobbyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping
    public String createLobby() {
        return lobbyService.createLobby();
    }

    @PostMapping("/{lobbyCode}/join")
    public void joinLobby(@PathVariable String lobbyCode, @RequestParam String teamLabel) {
        try {
            lobbyService.joinLobby(lobbyCode, teamLabel);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Erreur pour rejoindre le lobby : " + e.getMessage());
        }
    }
}