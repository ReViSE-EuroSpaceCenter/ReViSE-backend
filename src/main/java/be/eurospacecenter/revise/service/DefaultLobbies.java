package be.eurospacecenter.revise.service;

import be.eurospacecenter.revise.model.lobby.Host;
import be.eurospacecenter.revise.model.lobby.Lobby;
import be.eurospacecenter.revise.model.lobby.TeamLabel;
import be.eurospacecenter.revise.model.lobbycode.LobbyCode;
import be.eurospacecenter.revise.model.mission.MissionManager;
import be.eurospacecenter.revise.model.mission.MissionType;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Profile("dev")
public class DefaultLobbies implements CommandLineRunner {

    private static final LobbyCode LOBBY_CODE_FOUR_TEAMS = new LobbyCode("AAAAAA");
    private static final LobbyCode LOBBY_CODE_SIX_TEAMS = new LobbyCode("BBBBBB");

    private static final UUID FOUR_TEAMS_HOST_ID = UUID.fromString("12345678-1234-1234-1234-4444444444f1");
    private static final UUID SIX_TEAMS_HOST_ID = UUID.fromString("12345678-1234-1234-1234-6666666666f1");

    private static final int FOUR_TEAMS = 4;
    private static final int SIX_TEAMS = 6;

    private static final Set<UUID> CLIENT_IDS = Set.of(
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
        setupLobby(LOBBY_CODE_FOUR_TEAMS, FOUR_TEAMS_HOST_ID, FOUR_TEAMS);
        setupLobby(LOBBY_CODE_SIX_TEAMS, SIX_TEAMS_HOST_ID, SIX_TEAMS);
    }

    private void setupLobby(LobbyCode lobbyCode, UUID hostId, int teamCount) {
        Lobby lobby = new Lobby(new Host(hostId), teamCount, LocalDateTime.now().plusYears(10));
        lobbyService.addLobby(lobbyCode, lobby);

        Set<TeamLabel> allowedLabels = TeamLabel.getAllowedLabels(teamCount == FOUR_TEAMS);

        Iterator<UUID> clientIterator = CLIENT_IDS.iterator();
        Iterator<TeamLabel> labelIterator = allowedLabels.iterator();

        for (int i = 0; i < teamCount; i++) {
            UUID clientId = clientIterator.next();
            TeamLabel label = labelIterator.next();

            lobby.addTeam(clientId);
            lobby.assignTeam(clientId, label);
        }

        lobbyService.startGame(lobbyCode, hostId);

        MissionManager missionManager = missionService.getManager(lobbyCode);

        Set<MissionType> classicMissions = MissionType.getClassicMissions();
        Set<MissionType> firstSevenMissions = new LinkedHashSet<>();

        Iterator<MissionType> missionIterator = classicMissions.iterator();
        for (int i = 0; i < 7 && missionIterator.hasNext(); i++) {
            firstSevenMissions.add(missionIterator.next());
        }

        clientIterator = CLIENT_IDS.iterator();

        for (int i = 0; i < teamCount; i++) {
            UUID clientId = clientIterator.next();
            TeamLabel label = missionManager.getGameInfo().getTeams().get(clientId).getLabel();

            if (label == TeamLabel.MECA) {
                missionManager.changeTeamMissionsState(clientId, classicMissions);
            } else {
                missionManager.changeTeamMissionsState(clientId, firstSevenMissions);
            }
        }
    }
}