package be.eurospacecenter.revise.model;

public class Resource {
    private int remaining;

    public Resource(int initial) {
        this.remaining = initial;
    }

    public void remove(int amount) {
        int newRemaining = remaining - amount;
        if (amount < 0 || newRemaining < 0) {
            throw new IllegalArgumentException("La valeur ne peut pas devenir négative");
        }
        remaining = newRemaining;
    }

    public int remaining() {
        return remaining;
    }
}

