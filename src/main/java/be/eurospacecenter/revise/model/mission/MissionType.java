package be.eurospacecenter.revise.model.mission;

import java.util.EnumSet;
import java.util.Set;

public enum MissionType {
    CLASSIC_1,
    CLASSIC_2,
    CLASSIC_3,
    CLASSIC_4,
    CLASSIC_5,
    CLASSIC_6,
    CLASSIC_7,
    CLASSIC_8,
    BONUS_1,
    BONUS_2;

    private static final Set<MissionType> CLASSIC_MISSIONS = EnumSet.range(CLASSIC_1, CLASSIC_8);

    public static Set<MissionType> getClassicMissions() {
        return CLASSIC_MISSIONS;
    }
}
