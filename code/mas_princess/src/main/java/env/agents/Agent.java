package env.agents;

import env.objects.resources.Resource;
import env.utils.Pose;
public abstract class Agent extends jason.asSemantics.Agent {
    private String name;
    private boolean team;  // True for team 1, false for team 2
    private String state;
    private int hp;                   // Health points
    private int max_hp;
    private int attackRange;          // Attack range
    private int attackPower;          // Attack power
    private double landProbability;          // Attack power
    private Pose pose;                // Agent's position and orientation
    private Resource carriedResource;

    // Constructor
    public Agent(String name, boolean team, int max_hp, int attackRange, int attackPower, double landProbability) {
        this.name = name;
        this.team = team;
        this.state = "spawn";
        this.hp = max_hp;
        this.max_hp = max_hp;
        this.attackRange = attackRange;
        this.attackPower = attackPower;
        this.landProbability = landProbability;
        this.pose = null;  // Set the initial position and orientation
        this.carriedResource = null;
    }

    // Getters and Setters
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getTeam() {
        return team;
    }

    public void setTeam(boolean team) {
        this.team = team;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return max_hp;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public double getLandProbability() {
        return landProbability;
    }
    public void setLandProbability(double landProbability) {
        this.landProbability = landProbability;
    }

    public Resource getCarriedItem() { return this.carriedResource; }
    public void startCarrying(Resource carriedResource) {
        if (this.getCarriedItem() == null) {
            carriedResource.pickedUp();
            this.carriedResource = carriedResource; }
        }
    public void stopCarrying(Resource carriedResource) {
        if (this.getCarriedItem() != null) {
            carriedResource.dropped();
            this.carriedResource = null;
        }
    }
    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    // Utility Methods
    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) {
            this.hp = 0;  // Prevent negative HP
        }
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    // Attack Method
    public void attack(Agent target) {
        if (target != null && this.isAlive()) {
            // Apply attack power to target
            target.takeDamage(this.attackPower);
        }
    }

    @Override
    public String toString() {
        return String.format("Agent[Name: %s, Class: %s, Team: %s, State: %s, HP: %d, Attack Range: %d, Attack Power: %d, Pose: %s]",
                name, this.getClass().getSimpleName(), (team ? "Team 1" : "Team 2"), state, hp, attackRange, attackPower, pose);
    }
}
