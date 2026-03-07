package be.eurospacecenter.revise.helper;

import be.eurospacecenter.revise.model.Host;
import be.eurospacecenter.revise.model.Lobby;
import be.eurospacecenter.revise.model.TeamLabel;
import be.eurospacecenter.revise.service.LobbyService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev")
public class DefaultLobbies implements CommandLineRunner {

    private static final String LOBBY_CODE_FOUR_TEAMS = "AAAAAA";
    private static final String LOBBY_CODE_SIX_TEAMS = "BBBBBB";

    private static final UUID FOUR_TEAMS_HOST_ID = UUID.fromString("12345678-1234-1234-1234-4444444444f1");
    private static final UUID SIX_TEAMS_HOST_ID = UUID.fromString("12345678-1234-1234-1234-6666666666f1");

    private static final int FOUR_TEAMS = 4;
    private static final int SIX_TEAMS = 6;

    private static final List<UUID> CLIENT_IDS = List.of(
            UUID.fromString("12345678-1234-1234-1234-0000000000c1"),
            UUID.fromString("12345678-1234-1234-1234-0000000000c2"),
            UUID.fromString("12345678-1234-1234-1234-0000000000c3"),
            UUID.fromString("12345678-1234-1234-1234-0000000000c4"),
            UUID.fromString("12345678-1234-1234-1234-0000000000c5"),
            UUID.fromString("12345678-1234-1234-1234-0000000000c6"));

    private final LobbyService lobbyService;

    public DefaultLobbies(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @Override
    public void run(String @NonNull ... args) {
        setupFourTeams(lobbyService);
        setupSixTeams(lobbyService);
    }

    private void setupFourTeams(LobbyService lobbyService) {
        Lobby lobby = new Lobby(new Host(FOUR_TEAMS_HOST_ID), FOUR_TEAMS, LocalDateTime.now().plusYears(10));

        lobbyService.addLobby(LOBBY_CODE_FOUR_TEAMS, lobby);

        for (int i = 0; i < FOUR_TEAMS; i++) {
            lobby.addTeam(CLIENT_IDS.get(i));
            lobby.assignTeam(CLIENT_IDS.get(i), TeamLabel.getAllowedLabels(true).stream().toList().get(i).toString());
        }

        lobbyService.startGame(LOBBY_CODE_FOUR_TEAMS, FOUR_TEAMS_HOST_ID);
    }

    private void setupSixTeams(LobbyService lobbyService) {
        Lobby lobby = new Lobby(new Host(SIX_TEAMS_HOST_ID), SIX_TEAMS, LocalDateTime.now().plusYears(10));

        lobbyService.addLobby(LOBBY_CODE_SIX_TEAMS, lobby);

        for (int i = 0; i < SIX_TEAMS; i++) {
            lobby.addTeam(CLIENT_IDS.get(i));
            lobby.assignTeam(CLIENT_IDS.get(i), TeamLabel.getAllowedLabels(false).stream().toList().get(i).toString());
        }

        lobbyService.startGame(LOBBY_CODE_SIX_TEAMS, SIX_TEAMS_HOST_ID);
    }
}
