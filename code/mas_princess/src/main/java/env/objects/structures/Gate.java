package env.objects.structures;

// Gate structure
public class Gate extends MapStructure {
    private final int teamOwnership;

    public Gate(int maxLife, int teamOwnership) {
        super(true, true, 1, 2, maxLife); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
        this.teamOwnership = teamOwnership;
    }

    public int getTeamOwnership() {
        return teamOwnership;
    }

    @Override
    public void takeDamage(int damage) {
        // Gates only take damage from the opposing team
        super.takeDamage(damage);
    }
}
