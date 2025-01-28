package env.agents;

public abstract class Soldier extends Agent {
    // Constructor
    public Soldier(String name, boolean team, int max_hp, int attackRange, int attackPower) {
        super(name, team, max_hp, attackRange, attackPower);
    }
}
