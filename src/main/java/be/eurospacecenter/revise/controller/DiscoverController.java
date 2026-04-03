package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.EndDTO;
import be.eurospacecenter.revise.dto.request.ResourceUpdateDTO;
import be.eurospacecenter.revise.dto.discover.ScoreDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.service.DiscoverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/discover")
public class DiscoverController {

    private final DiscoverService discoverService;

    public DiscoverController(DiscoverService discoverService) {
        this.discoverService = discoverService;
    }

    @GetMapping(value = "/{lobbyCode}/score", params = "hostId")
    public ScoreDTO getTeamsScore(
            @PathVariable
            String lobbyCode,

            @RequestParam @Valid
            UUID hostId
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);

        int score = discoverService.getTeamsScore(code, hostId);

        return new ScoreDTO(score);
    }

    @PutMapping("/{lobbyCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResources(
            @PathVariable
            String lobbyCode,

            @RequestBody
            ResourceUpdateDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        discoverService.updateResources(code, request.clientId(), request.resources());
    }

    @PostMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endMission(
            @PathVariable String lobbyCode,

            @RequestBody @Valid
            EndDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        discoverService.endDiscover(code, request.hostId());
    }
}
