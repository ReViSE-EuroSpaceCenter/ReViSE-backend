package be.eurospacecenter.revise.model;

public enum TeamId {
    COOP("COOP"), EXPE("EXPE"), GECO("GECO"), INGE("INGE"), MECA("MECA"), MEDI("MEDI");

    public final String label;

    TeamId(String label) {
        this.label = label;
    }
}
