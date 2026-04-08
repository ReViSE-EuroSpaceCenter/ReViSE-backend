package be.eurospacecenter.revise.controller;

import be.eurospacecenter.revise.dto.request.HostIdDTO;
import be.eurospacecenter.revise.dto.request.ResourceUpdateDTO;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/{lobbyCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResource(
            @PathVariable
            String lobbyCode,

            @RequestBody
            ResourceUpdateDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        resourceService.updateResource(code, request.clientId(), request.resources());
    }

    @PutMapping("/{lobbyCode}/end")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void endResourceEncoding(
            @PathVariable
            String lobbyCode,

            @RequestBody
            HostIdDTO request
    ) {
        LobbyCode code = new LobbyCode(lobbyCode);
        resourceService.endEncodingResources(code, request.hostId());
    }
}
