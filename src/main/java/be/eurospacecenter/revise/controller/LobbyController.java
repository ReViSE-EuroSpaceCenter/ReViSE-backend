package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.service.LobbyService;
import org.springframework.web.bind.annotation.*;

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
        lobbyService.joinLobby(lobbyCode, teamLabel);
    }
}