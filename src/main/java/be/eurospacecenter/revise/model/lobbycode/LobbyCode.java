package be.eurospacecenter.revise.model.lobbycode;

import be.eurospacecenter.revise.exceptions.ErrorKeys;

public record LobbyCode(String lobbyCode) {
    public static final String PATTERN = "^[A-Z]{6}$";

    public LobbyCode {
        if (!lobbyCode.matches(PATTERN)) {
            throw new IllegalArgumentException(ErrorKeys.INVALID_LOBBY_CODE);
        }
    }
}
