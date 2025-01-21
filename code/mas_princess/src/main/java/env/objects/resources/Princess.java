package env.objects.resources;

public class Princess extends Resource {
    private boolean team; // Current weight of the princess
    private int weight; // Current weight of the princess
    private final int maxWeight; // Maximum weight the princess can achieve
    private final int minWeight; // Minimum weight before she stops losing weight
    private final int weightDecayRate; // How much weight she loses per timestep if not fed
    private int weightDecayTimer; // Counter for weight decay

    private boolean isCarried; // Whether the princess is currently being carried
    private int carryingTeam; // The team currently carrying the princess (if any)

    public Princess(boolean team, int x, int y, int initialWeight, int maxWeight, int minWeight, int weightDecayRate) {
        super(1, x, y); // Princess is not breakable, not repairable, and has no life points
        this.team = team;
        this.weight = initialWeight;
        this.maxWeight = maxWeight;
        this.minWeight = minWeight;
        this.weightDecayRate = weightDecayRate;
        this.weightDecayTimer = 0;
        this.isCarried = false;
        this.carryingTeam = -1; // No team is carrying her initially
    }

    public int getWeight() {
        return weight;
    }

    public boolean isCarried() {
        return isCarried;
    }

    public int getCarryingTeam() {
        return carryingTeam;
    }

    public void feed(int foodAmount) {
        if (!isCarried) { // Only feed if she's not being carried
            weight = Math.min(weight + foodAmount, maxWeight);
        }
    }

    public void pickUp(int team) {
        if (!isCarried) {
            isCarried = true;
            carryingTeam = team;
        }
    }

    public void drop() {
        isCarried = false;
        carryingTeam = -1;
    }

    public void updateWeightDecay() {
        if (!isCarried) { // Weight decays only if the princess is not carried
            weightDecayTimer++;
            if (weightDecayTimer >= weightDecayRate) {
                weight = Math.max(weight - 1, minWeight);
                weightDecayTimer = 0; // Reset the timer
            }
        }
    }
}
