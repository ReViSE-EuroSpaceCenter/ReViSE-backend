package be.eurospacecenter.revise.dto;

public class LobbyResponse {
    private String lobbyCode;

    public LobbyResponse(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }
}
