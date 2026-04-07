package be.eurospacecenter.revise.exceptions;

public final class ErrorKeys {

    private ErrorKeys() {}

    // General
    public static final String ACTION_RESERVED_TO_HOST = "actionReservedToHost";
    public static final String INVALID_LOBBY_CODE = "invalidLobbyCode";
    public static final String INVALID_UUID = "invalidUuid";
    public static final String TEAM_NOT_FOUND = "teamNotFound";

    // State
    public static final String INVALID_GAME_STATE_TRANSITION = "invalidGameStateTransition";
    public static final String INVALID_GAME_STATE = "invalidGameState";

    // Lobby
    public static final String LOBBY_MANAGER_NOT_FOUND = "lobbyNotFound";
    public static final String CLIENT_NOT_IN_LOBBY = "clientNotInLobby";
    public static final String CLIENT_ALREADY_CHOSE_TEAM = "clientAlreadyChooseTeam";
    public static final String INVALID_NUMBER_OF_TEAMS = "invalidNumberOfTeams";
    public static final String INVALID_TEAM_LABEL = "invalidTeamLabel";
    public static final String INVALID_TEAM_LABELS = "invalidTeamLabels";
    public static final String TEAM_LABEL_ALREADY_TAKEN = "teamLabelAlreadyTaken";

    // Mission
    public static final String MISSION_MANAGER_NOT_FOUND = "gameNotFound";
    public static final String INVALID_MISSION_TYPE = "invalidMissionType";
    public static final String ONLY_MECA_COMPLETE_CLASSIC_8 = "onlyMecaCanCompleteClassic8";
    public static final String DISCOVER_START_INCOMPLETE_MISSIONS = "discoverStartIncompleteMissions";

    // Discover
    public static final String DISCOVER_MANAGER_NOT_FOUND = "discoverNotFound";
    public static final String INVALID_RESOURCE_TYPE = "invalidResourceType";
    public static final String INSUFFICIENT_RESOURCES = "insufficientResources";

}
