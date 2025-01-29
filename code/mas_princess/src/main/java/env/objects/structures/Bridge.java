package env.objects.structures;
import env.utils.*;

// Bridge structure
public class Bridge extends MapStructure {
    private final double slipProbability;

    public Bridge(double slipProbability, Pose pose) {
        super("bridge", false , false, true,3, 2, 1, null, pose); // Bridges are breakable, not repairable, 3 cells wide, 2 cells tall, and have 1 life point
        this.slipProbability = slipProbability;
    }

}
