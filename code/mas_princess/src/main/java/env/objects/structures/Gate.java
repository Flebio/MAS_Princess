package env.objects.structures;

// Gate structure
public class Gate extends MapStructure {

    public Gate(int maxLife, Boolean team) {
        super(true, true, true,1, 2, maxLife, team); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
    }

    @Override
    public void takeDamage(int damage) {
        // Gates only take damage from the opposing team
        super.takeDamage(damage);
    }
}
