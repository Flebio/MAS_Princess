package env.agents;

public class Priest extends Agent {
    int healPower = 20;

    public Priest(String name, boolean team) {
        super(name, team, 90, 2, 5, .95);
    }

    public int getHealPower() {
        return healPower;
    }

    public void setHealPower(int newHealPower) {
        this.healPower = newHealPower;
    }
}
