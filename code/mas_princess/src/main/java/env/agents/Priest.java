package env.agents;

/**
 * Represents a Priest agent in the game environment. Priests have specific attributes for
 * health, attack range, attack power, land probability and heal power.
 */
public class Priest extends Agent {
    private int healPower = 20;

    /**
     * Constructs a new Priest with the specified name and team. Sets the Priest's attributes
     * to predefined values.
     *
     * @param name the name of the priest.
     * @param team {@code true} if the priest belongs to team 1, {@code false} for team 2.
     */
    public Priest(String name, boolean team) {
        super(name, team, 90, 2, 5, 0.95);
    }

    /**
     * Returns the heal power of the priest.
     *
     * @return the heal power of the priest.
     */
    public int getHealPower() {
        return healPower;
    }

    /**
     * Sets the heal power of the priest.
     *
     * @param newHealPower the new heal power of the priest.
     */
    public void setHealPower(int newHealPower) {
        this.healPower = newHealPower;
    }
}