package env.agents;

/**
 * Represents a Warrior agent in the game environment. Warriors have specific attributes for
 * health, attack range, attack power, and land probability.
 */
public class Warrior extends Agent {
    /**
     * Constructs a new Warrior with the specified name and team. Sets the Warrior's attributes
     * to predefined values.
     *
     * @param name the name of the warrior.
     * @param team {@code true} if the warrior belongs to team 1, {@code false} for team 2.
     */
    public Warrior(String name, boolean team) {
        super(name, team, 100, 1, 15, 0.75);
    }
}