package be.eurospacecenter.revise.helper;

import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.mission.MissionManager;
import be.eurospacecenter.revise.model.mission.MissionType;
import be.eurospacecenter.revise.service.LobbyService;
import be.eurospacecenter.revise.service.MissionService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
            UUID.fromString("12345678-1234-1234-1234-0000000000c6")
    );

    private final LobbyService lobbyService;
    private final MissionService missionService;

    public DefaultLobbies(LobbyService lobbyService, MissionService missionService) {
        this.lobbyService = lobbyService;
        this.missionService = missionService;
    }

    @Override
    public void run(String @NonNull ... args) {
        setupLobby(LOBBY_CODE_FOUR_TEAMS, FOUR_TEAMS_HOST_ID, FOUR_TEAMS, true);
        setupLobby(LOBBY_CODE_SIX_TEAMS, SIX_TEAMS_HOST_ID, SIX_TEAMS, false);
    }

    private void setupLobby(String lobbyCode, UUID hostId, int teamCount, boolean allowedFourTeamsLabels) {
        Lobby lobby = new Lobby(new Host(hostId), teamCount, LocalDateTime.now().plusYears(10));
        lobbyService.addLobby(lobbyCode, lobby);

        List<TeamLabel> allowedLabels = new ArrayList<>(TeamLabel.getAllowedLabels(allowedFourTeamsLabels));

        for (int i = 0; i < teamCount; i++) {
            UUID clientId = CLIENT_IDS.get(i);
            lobby.addTeam(clientId);
            lobby.assignTeam(clientId, allowedLabels.get(i).toString());
        }

        lobbyService.startGame(lobbyCode, hostId);

        MissionManager missionManager = missionService.getManager(lobbyCode);
        List<MissionType> classicMissions = new ArrayList<>(MissionType.getClassicMissions());
        List<MissionType> firstSevenMissions = classicMissions.subList(0, Math.min(7, classicMissions.size()));

        for (int i = 0; i < teamCount; i++) {
            UUID clientId = CLIENT_IDS.get(i);

            if (i == 3) { // Meca
                missionManager.changeTeamMissionsState(clientId, classicMissions);
            } else {
                missionManager.changeTeamMissionsState(clientId, firstSevenMissions);
            }
        }
    }
}