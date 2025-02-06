package env.agents;

import env.objects.resources.Resource;
import env.utils.Pose;

/**
 * The base class for all agents in the environment. Defines common properties and behaviors
 * shared by all agents, such as name, team, state, health points, attack range, attack power,
 * pose, and carried resources.
 */
public abstract class Agent extends jason.asSemantics.Agent {
    private String name;
    private boolean team;  // True for team 1, false for team 2
    private String state;
    private int hp;                   // Health points
    private int max_hp;
    private int attackRange;          // Attack range
    private int attackPower;          // Attack power
    private double landProbability;    // Probability of choosing the land path
    private Pose pose;                // Agent's position and orientation
    private Resource carriedResource;

    /**
     * Constructs a new Agent with the specified properties.
     *
     * @param name            the name of the agent.
     * @param team            {@code true} if the agent belongs to team 1, {@code false} for team 2.
     * @param max_hp          the maximum health points of the agent.
     * @param attackRange     the attack range of the agent.
     * @param attackPower     the attack power of the agent.
     * @param landProbability the probability of the agent choosing the land path.
     */
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

    /**
     * Returns the name of the agent.
     *
     * @return the name of the agent.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the agent.
     *
     * @param name the new name of the agent.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the team the agent belongs to.
     *
     * @return {@code true} if the agent is on team 1, {@code false} for team 2.
     */
    public boolean getTeam() {
        return team;
    }

    /**
     * Sets the team the agent belongs to.
     *
     * @param team {@code true} if the agent is on team 1, {@code false} for team 2.
     */
    public void setTeam(boolean team) {
        this.team = team;
    }

    /**
     * Returns the current state of the agent.
     *
     * @return the current state of the agent.
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the current state of the agent.
     *
     * @param state the new state of the agent.
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the current health points of the agent.
     *
     * @return the current health points.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the current health points of the agent, clamping the value between 0 and max_hp.
     *
     * @param new_hp the new health points value.
     */
    public void setHp(int new_hp) {
        this.hp = Math.max(0, Math.min(new_hp, this.getMaxHp()));
    }

    /**
     * Returns the maximum health points of the agent.
     *
     * @return the maximum health points.
     */
    public int getMaxHp() {
        return max_hp;
    }

    /**
     * Returns the attack range of the agent.
     *
     * @return the attack range.
     */
    public int getAttackRange() {
        return attackRange;
    }

    /**
     * Returns the attack power of the agent.
     *
     * @return the attack power.
     */
    public int getAttackPower() {
        return attackPower;
    }

    /**
     * Sets the attack power of the agent.
     *
     * @param attackPower the new attack power.
     */
    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    /**
     * Returns the probability of the agent choosing the land path.
     *
     * @return the probability of choosing the land path.
     */
    public double getLandProbability() {
        return landProbability;
    }

    /**
     * Returns the resource currently carried by the agent.
     *
     * @return the carried resource, or {@code null} if the agent is not carrying anything.
     */
    public Resource getCarriedItem() {
        return this.carriedResource;
    }

    /**
     * Sets the resource carried by the agent.
     *
     * @param carriedResource the resource to be carried.
     * @throws IllegalStateException If the agent is already carrying a resource.
     */
    public void startCarrying(Resource carriedResource) {
        if (this.getCarriedItem() == null) {
            carriedResource.pickedUp();
            this.carriedResource = carriedResource;
        } else {
            throw new IllegalStateException("Agent is already carrying a resource.");
        }
    }

    /**
     * Stops the agent from carrying the resource.
     *
     * @param carriedResource the resource to stop carrying.
     * @throws IllegalStateException If the agent is not carrying the specified resource.
     */
    public void stopCarrying(Resource carriedResource) {
        if (this.getCarriedItem() != null) {
            if(this.getCarriedItem().equals(carriedResource)) {
                carriedResource.dropped();
                this.carriedResource = null;
            } else {
                throw new IllegalStateException("Agent is not carrying the specified resource.");
            }
        }
    }

    /**
     * Returns the pose (position and orientation) of the agent.
     *
     * @return the pose of the agent.
     */
    public Pose getPose() {
        return pose;
    }

    /**
     * Sets the pose (position and orientation) of the agent.
     *
     * @param pose the new pose for the agent.
     */
    public void setPose(Pose pose) {
        this.pose = pose;
    }

    /**
     * Returns a string representation of the agent.
     *
     * @return a string representation of the agent, including its name, class, team, state,
     *         health points, attack range, attack power, and pose.
     */
    @Override
    public String toString() {
        return String.format("Agent[Name: %s, Class: %s, Team: %s, State: %s, HP: %d, Attack Range: %d, Attack Power: %d, Pose: %s]",
                name, this.getClass().getSimpleName(), (team ? "Team 1" : "Team 2"), state, hp, attackRange, attackPower, pose);
    }
}