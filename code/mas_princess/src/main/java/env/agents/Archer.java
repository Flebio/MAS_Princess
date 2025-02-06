package env.agents;

/**
 * Represents an Archer agent in the game environment. Archers have specific attributes for
 * health, attack range, attack power, and land probability.
 */
public class Archer extends Agent {
    /**
     * Constructs a new Archer with the specified name and team.  Sets the Archer's attributes
     * to predefined values.
     *
     * @param name the name of the archer.
     * @param team {@code true} if the archer belongs to team 1, {@code false} for team 2.
     */
    public Archer(String name, boolean team) {
        super(name, team, 80, 2, 18, 0.75);
    }
}