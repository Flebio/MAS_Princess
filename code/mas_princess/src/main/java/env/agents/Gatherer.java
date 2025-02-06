package env.agents;

/**
 * Represents a Gatherer agent in the game environment. Gatherers have specific attributes for
 * health, attack range, attack power, and land probability.
 */
public class Gatherer extends Agent {
    /**
     * Constructs a new Gatherer with the specified name and team. Sets the Gatherer's attributes
     * to predefined values.
     *
     * @param name the name of the gatherer.
     * @param team {@code true} if the gatherer belongs to team 1, {@code false} for team 2.
     */
    public Gatherer(String name, boolean team) {
        super(name, team, 60, 1, 5, 0.10);
    }
}