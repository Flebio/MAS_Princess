package env;

import env.agents.*;
import env.utils.*;
import env.objects.structures.*;
import env.objects.resources.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The `BlackForestGameMap` class represents the spatial structure of the game environment.
 * It contains the methods and data structures used by `BlackForestModel` to manage terrain,
 * resources, structures and agents.
 *
 * This class provides essential functionalities such as pathfinding, obstacle detection,
 * and environmental state updates, making it a key component for the simulation logic.
 */
public class BlackForestGameMap {
    private Boolean win = null;
    private final int width, height, baseWidth, baseHeight, enoughWoodAmount = 5;
    private Vector2D bluePrincessSpawnPoint = null, redPrincessSpawnPoint = null;
    private final AtomicInteger woodAmountBlue = new AtomicInteger(0), woodAmountRed = new AtomicInteger(0);
    private final Cell[][] map;
    private final Map<String, Agent> agentsList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, MapStructure> structuresList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Resource> resourcesList = Collections.synchronizedMap(new HashMap<>());
    private static final Random RAND = new Random();
    private MapView view;

    public BlackForestGameMap(int width, int height, MapView view) {
        this.width = Objects.requireNonNull(width);
        this.height = Objects.requireNonNull(height);
        this.map = new Cell[width][height];
        this.view = view;
        this.baseWidth = this.getWidth() / 6;
        this.baseHeight = this.getHeight() / 4;
        createZones();
        addStructures();
        addResources();
    }

    /**
     * Returns the width of the game map.
     *
     * @return The width of the map.
     */
    public int getWidth() {
        return width;
    }
    /**
     * Returns the height of the game map.
     *
     * @return The height of the map.
     */
    public int getHeight() {
        return height;
    }

    // Agent management and acts
    /**
     * Checks if the given agent exists in the agents list.
     *
     * @param agent The agent to check.
     * @return {@code true} if the agent is present, {@code false} otherwise.
     */
    private synchronized boolean containsAgent(Agent agent) {
        synchronized (this.agentsList) {
            return this.agentsList.containsKey(agent.getName());
        }
    }
    /**
     * Determines whether two agents are within a specified range of each other.
     *
     * @param agent The first agent.
     * @param neighbour The second agent.
     * @param range The maximum distance for them to be considered neighbors.
     * @return {@code true} if the agents are within range and eligible as neighbors, {@code false} otherwise.
     */
    private boolean areAgentsNeighbours(Agent agent, Agent neighbour, int range) {
        if (!containsAgent(agent) || !containsAgent(neighbour)) {
            return false;
        }

        if ((agent instanceof Priest) & agent.getTeam() == neighbour.getTeam() & neighbour.getHp() == neighbour.getMaxHp()) {
            return false;
        }

        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = this.getAgentPosition(neighbour);
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }
    /**
     * Ensures that the given agent exists in the agents list.
     * Throws an exception if the agent is not found.
     *
     * @param agent The agent to verify.
     * @throws IllegalArgumentException If the agent does not exist.
     */
    private void ensureAgentExists(Agent agent) {
        if (!containsAgent(agent)) {
            throw new IllegalArgumentException("No such agent: " + agent.getName());
        }
    }
    /**
     * Retrieves the position of the given agent.
     *
     * @param agent The agent whose position is to be retrieved.
     * @return The position of the agent as a {@link Vector2D}.
     * @throws IllegalArgumentException If the agent does not exist.
     */
    private synchronized Vector2D getAgentPosition(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getPosition();
        }
    }
    /**
     * Retrieves the direction (orientation) of the given agent.
     *
     * @param agent The agent whose direction is to be retrieved.
     * @return The orientation of the agent.
     * @throws IllegalArgumentException If the agent does not exist.
     */
    private synchronized Orientation getAgentDirection(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getOrientation();
        }
    }
    /**
     * Finds an agent located at a specific position.
     *
     * @param position The position to check.
     * @return An {@link Optional} containing the agent if found, or an empty {@link Optional} if no agent is present at the position.
     */
    private synchronized Optional<Agent> getAgentByPosition(Vector2D position) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(agent -> agent.getPose().getPosition().equals(position))
                    .findFirst();
        }
    }
    /**
     * Sets the position of the given agent.
     *
     * @param agent The agent whose position is to be updated.
     * @param position The new position of the agent.
     */
    private synchronized void setAgentPosition(Agent agent, Vector2D position) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        agent.setPose(new Pose(position, currentPose.getOrientation()));
        this.agentsList.put(agent.getName(), agent);
    }
    /**
     * Sets the direction (orientation) of the given agent.
     *
     * @param agent The agent whose direction is to be updated.
     * @param orientation The new orientation of the agent.
     */
    private synchronized void setAgentDirection(Agent agent, Orientation orientation) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        agent.setPose(new Pose(currentPose.getPosition(), orientation));
        this.agentsList.put(agent.getName(), agent);
    }
    /**
     * Updates the pose (position and direction) of an agent if the position is valid and unoccupied.
     *
     * @param agent The agent whose pose is being set.
     * @param x The x-coordinate of the new position.
     * @param y The y-coordinate of the new position.
     * @param orientation The new orientation of the agent.
     * @return {@code true} if the pose was successfully updated, {@code false} otherwise.
     */
    private synchronized boolean setAgentPose(Agent agent, int x, int y, Orientation orientation) {
        if (this.containsAgent(agent)) {
            this.setAgentDirection(agent, orientation);
            if (this.isPositionInside(x, y)) {
                Vector2D position = Vector2D.of(x, y);
                if (!this.getAgentByPosition(position).isPresent()) {
                    this.setAgentPosition(agent, position);
                    return true;
                }
            }
            return false;
        }
        Cell newCell = this.getCellByPosition(x, y);
        if (!newCell.isOccupied(agent, null)) {
            newCell.setAgent(agent);
        }

        agent.setPose(new Pose(Vector2D.of(x, y), orientation));
        this.agentsList.put(agent.getName(), agent);

        return true;
    }
    /**
     * Updates the pose (position and direction) of an agent using a {@link Vector2D} position.
     *
     * @param agent The agent whose pose is being set.
     * @param afterStep The new position of the agent.
     * @param newOrientation The new orientation of the agent.
     * @return {@code true} if the pose was successfully updated, {@code false} otherwise.
     */
    private synchronized boolean setAgentPose(Agent agent, Vector2D afterStep, Orientation newOrientation) {
        return this.setAgentPose(agent, afterStep.getX(), afterStep.getY(), newOrientation);
    }
    /**
     * Removes the specified agent from the map and updates resource placement if the agent was carrying an item.
     *
     * @param agent The agent to remove.
     * @return True if the agent was successfully removed, false otherwise.
     */
    private synchronized boolean removeAgent(Agent agent) {
        Cell agentCell = this.getCellByPosition(agent.getPose().getPosition());
        if (agentCell != null) {
            if (agentCell.getAgent() != null && agentCell.getAgent().getCarriedItem() != null) {
                synchronized (this.resourcesList) {

                    agentCell.setResource(agentCell.getAgent().getCarriedItem());
                    agentCell.getAgent().stopCarrying(agentCell.getAgent().getCarriedItem());

                    this.resourcesList.put(agentCell.getResource().getName(), agentCell.getResource());
                }
            }
            agentCell.clearAgent();

            return true;
        }

        return false;
    }
    /**
     * Retrieves a set of all agents currently present on the map.
     *
     * @return A set containing all agents.
     */
    public synchronized Set<Agent> getAllAgents() {
        synchronized (this.agentsList) {
            return this.agentsList.values()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }
    /**
     * Spawns an agent on a random cell at a designated team base. If its HP is less than 0,
     * the agent is marked as dead and removed.
     *
     * @param agent The agent to spawn.
     * @return True if the agent was successfully spawned, false otherwise.
     */
    public synchronized boolean spawnAgent(Agent agent) {
        boolean result;
        synchronized (this.map) {
            synchronized (this.agentsList) {
                // If the agent's HP is 0 or less, perform respawn logic
                if (agent.getHp() <= 0) {
                    agent.setState("dead");
                    this.removeAgent(agent);
                } else {
                    agent.setState("spawn");
                }
                Cell randomCell = getRandomCell(agent, (agent.getTeam() == true) ? Zone.RBASE : Zone.BBASE, false);

                if (randomCell != null) {
                    result = setAgentPose(agent, randomCell.getX(), randomCell.getY(), Orientation.random());
                } else {
                    result = false;
                }

                return result;
            }
        }
    }
    /**
     * Resets an agent's state by restoring its HP to the maximum and setting it to the spawn state.
     *
     * @param agent The agent to reset.
     * @return True if the agent was successfully reset.
     */
    public synchronized boolean resetAgent (Agent agent) {
        synchronized (this.agentsList) {
            agent.setHp(agent.getMaxHp());
            agent.setState("spawn");
            return true;
        }
    }
    /**
     * Moves an agent to a new position with a specified orientation, ensuring that the movement is valid.
     *
     * @param agent The agent to move.
     * @param newPosition The target position.
     * @param newOrientation The target orientation.
     * @return True if the agent was successfully moved, false otherwise.
     */
    public synchronized boolean moveAgent(Agent agent, Vector2D newPosition, Orientation newOrientation) {
        synchronized (this.map) {
            synchronized (this.structuresList) {
                synchronized (this.agentsList) {

                    if (!isPositionInside(newPosition.getX(), newPosition.getY())) {
                        return false;
                    }

                    Pose currentPose = agentsList.get(agent.getName()).getPose();

                    Cell currentCell = this.getCellByPosition(currentPose.getPosition().getX(), currentPose.getPosition().getY());
                    Cell targetCell = this.getCellByPosition(newPosition.getX(), newPosition.getY());

                    if (targetCell.getZoneType() == Zone.OUT_OF_MAP) {
                        return false;
                    }

                    if (targetCell.isOccupied(agent, null)) {
                        return false;  // Target cell is occupied
                    }

                    // Update agent position
                    this.setAgentPose(agent, newPosition, newOrientation);

                    currentCell.setAgent(null);
                    targetCell.setAgent(agent);

                    if (agent.getCarriedItem() != null) {
                        synchronized (this.resourcesList) {

                            agent.getCarriedItem().setPose(new Pose(agent.getPose().getPosition(), Orientation.SOUTH));
                            currentCell.setResource(null);
                            targetCell.setResource(agent.getCarriedItem());

                            this.resourcesList.put(agent.getCarriedItem().getName(), agent.getCarriedItem());

                        }
                    }

                    return true;
                }
            }
        }
    }
    /**
     * Moves an agent by a given step size in a specified direction.
     *
     * @param agent The agent to move.
     * @param stepSize The number of steps to move.
     * @param direction The direction in which to move.
     * @return True if the agent was successfully moved, false otherwise.
     */
    public synchronized boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        Orientation newOrientation = currentPose.getOrientation().rotate(direction);

        // Delegate to the main method
        return moveAgent(agent, currentPose.getPosition().afterStep(stepSize, newOrientation), newOrientation);
    }
    /**
     * Makes an agent attack another agent, optionally performing a critical hit.
     *
     * @param attacking_agent The attacking agent.
     * @param target The target agent.
     * @param crit Whether the attack is a critical hit.
     * @return True if the attack was successful, false otherwise.
     */
    public synchronized boolean attackAgent(Agent attacking_agent, Agent target, boolean crit) {
        synchronized (this.agentsList) {
            if (target.getHp() > 0) {
                int originalAttackPower = attacking_agent.getAttackPower();

                if (crit) {
                    attacking_agent.setAttackPower(originalAttackPower * 5);
                }

                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                int newHp = target.getHp() - attacking_agent.getAttackPower();
                target.setHp(newHp);
                if (crit) {
                    attacking_agent.setAttackPower(originalAttackPower);
                }
                return true;

            } else {
                return false; }
            }
        }
    /**
     * Allows an agent to heal another agent if they are not already at full HP.
     *
     * @param healing_agent The agent performing the healing.
     * @param target The agent receiving the healing.
     * @return True if the healing was successful, false otherwise.
     */
    public synchronized boolean healAgent(Agent healing_agent, Agent target) {
        synchronized (this.agentsList) {
            if ((target.getHp()) > 0 && (target.getHp() < target.getMaxHp())) {
                view.triggerAttackView(healing_agent.getPose().getPosition());
                view.triggerHealView(target.getPose().getPosition());

                int newHp = 0;
                if ((healing_agent instanceof Priest priest)) {
                    newHp = target.getHp() + priest.getHealPower();
                }

                target.setHp(newHp);
                return true;
            } else {
                return false;
            }
        }
    }
    /**
     * Makes an agent attack a gate, dealing damage to it.
     *
     * @param attacking_agent The agent attacking the gate.
     * @param target The gate being attacked.
     * @return True if the attack was successful, false otherwise.
     */
    public synchronized boolean attackGate(Agent attacking_agent, Gate target) {
        synchronized (this.structuresList) {
            if (target.getHp() > 0) {
                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                target.takeDamage(attacking_agent.getAttackPower());
                return true;
            } else {
                return false;
            }
        }
    }
    /**
     * Allows an agent (Instance of Gatherer) to repair a damaged or destroyed gate if sufficient resources are available.
     *
     * @param repairing_agent The agent repairing the gate.
     * @param target The gate to repair.
     * @return True if the gate was successfully repaired, false otherwise.
     */
    public synchronized boolean repairGate(Agent repairing_agent, Gate target) {
        synchronized (this.structuresList) {
            boolean isBlueTeam = !repairing_agent.getTeam();
            boolean isRedTeam = repairing_agent.getTeam();

            if (target.isDestroyed()) {
                if (isBlueTeam) {
                    synchronized (this.getWoodAmountBlue()) {
                        if (this.getWoodAmountBlue().get() >= this.enoughWoodAmount) {
                            target.repair();
                            this.woodAmountBlue.addAndGet(-this.enoughWoodAmount);
                            return true;
                        }
                    }
                } else if (isRedTeam) {
                    synchronized (this.getWoodAmountRed()) {
                        if (this.getWoodAmountRed().get() >= this.enoughWoodAmount) {
                            target.repair();
                            this.woodAmountRed.addAndGet(-this.enoughWoodAmount);
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }
    /**
     * Makes an agent (Instance of Gatherer) attack a tree, reducing its HP and potentially converting it into wood resources.
     *
     * @param attacking_agent The agent attacking the tree.
     * @param target The tree being attacked.
     * @return True if the attack was successful, false otherwise.
     */
    public synchronized boolean attackTree(Agent attacking_agent, Tree target) {
        synchronized (this.structuresList) {
            if (target.getHp() > 0) {
                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                target.takeDamage(attacking_agent.getAttackPower());
                if (target.getHp() == 0) {
                    addWood(attacking_agent);
                }

                return true;
            } else {
                return false;
            }
        }
    }
    /**
     * Allows an agent to pick up a princess if she is not already being carried.
     *
     * @param agent The agent attempting to pick up the princess.
     * @param target The princess being picked up.
     * @return True if the princess was successfully picked up, false otherwise.
     */
    public synchronized boolean pickUpPrincess(Agent agent, Princess target) {
        synchronized (this.map) {
            synchronized (agentsList) {
                synchronized (resourcesList) {
                    if (!target.isCarried()) {
                        Vector2D p_pos = target.getPose().getPosition();
                        agent.startCarrying(target);
                        target.setPose(agent.getPose());
                        this.getCellByPosition(p_pos).clearResource();
                        this.getCellByPosition(agent.getPose().getPosition()).setResource(target);

                        this.agentsList.put(agent.getName(), agent);
                        this.resourcesList.put(target.getName(), target);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }
    /**
     * Retrieves an agent by its name.
     *
     * @param agName The name of the agent.
     * @return An optional containing the agent if found, or empty if no agent matches the name.
     */
    public synchronized Optional<Agent> getAgentByName(String agName) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(entry -> agName.equals(entry.getName()))
                    .findFirst();
        }
    }
    /**
     * Retrieves a set of agents that are within a specified range of the given agent.
     *
     * @param agent The reference agent.
     * @param range The maximum distance to check.
     * @return A set of neighboring agents.
     */
    public Set<Agent> getAgentNeighbours(Agent agent, int range) {
        return this.getAllAgents().stream()
                .filter(it -> it.getHp() > 0)
                .filter(it -> !it.equals(agent))
                .filter(other -> this.areAgentsNeighbours(agent, other, range))
                .collect(Collectors.toSet());
    }
    /**
     * Retrieves a set of gates that are within a specified range of the given agent, based on team affiliation.
     *
     * @param agent The reference agent.
     * @param team The team filter ("enemy" or "ally").
     * @param range The maximum distance to check.
     * @return A set of gates matching the criteria.
     */
    public Set<Gate> getGateNeighbours(Agent agent, String team, int range) {
        boolean isBlueTeam = !agent.getTeam(); // True if the agent is on the blue team
        boolean isRedTeam = agent.getTeam();   // True if the agent is on the red team
        boolean isEnemyTeam = team.equals("enemy"); // True if searching for enemy gates
        boolean isAllyTeam = team.equals("ally");   // True if searching for ally gates
        boolean hasEnoughWoodBlue = this.isEnoughWoodBlue(); // Blue team has enough wood
        boolean hasEnoughWoodRed = this.isEnoughWoodRed();   // Red team has enough wood
        boolean hasEnoughWood = (isBlueTeam && hasEnoughWoodBlue) || (isRedTeam && hasEnoughWoodRed); // Current team has enough wood

        if (isEnemyTeam) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(gate -> !gate.getTeam().equals(agent.getTeam())) // Only enemy gates
                    .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .collect(Collectors.toSet());
        } else if (isAllyTeam && hasEnoughWood) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(gate -> gate.getTeam().equals(agent.getTeam())) // Only ally gates
//                    .filter(gate -> this.getCellByPosition(gate.getPose().getPosition()).isOccupied(agent, null))
                    .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .collect(Collectors.toSet());
        }

        return this.getAllStructures(Gate.class).stream()
                .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                .map(gate -> (Gate) gate) // Cast to Gate
                .collect(Collectors.toSet());
    }
    /**
     * Retrieves a set of trees that are within a specified range of the given agent.
     *
     * @param agent The reference agent.
     * @param range The maximum distance to check.
     * @return A set of neighboring trees.
     */
    public Set<Tree> getTreeNeighbours(Agent agent, int range) {
        boolean isBlueTeam = !agent.getTeam(); // True if the agent is on the blue team
        boolean isRedTeam = agent.getTeam();   // True if the agent is on the red team

        // Gate destruction status
        boolean isBlueGateDestroyed = this.getGateByName("gate_b1").get().isDestroyed() ||
                this.getGateByName("gate_b2").get().isDestroyed();
        boolean isRedGateDestroyed = this.getGateByName("gate_r1").get().isDestroyed() ||
                this.getGateByName("gate_r2").get().isDestroyed();
        boolean isAnyGateDestroyed = isBlueGateDestroyed || isRedGateDestroyed;

        // Princess carrying status
        boolean isBluePrincessCarried = this.getPrincessByName("princess_b").get().isCarried();
        boolean isRedPrincessCarried = this.getPrincessByName("princess_r").get().isCarried();
        boolean isAnyPrincessCarried = isBluePrincessCarried || isRedPrincessCarried;

        // Wood availability
        boolean hasEnoughWoodBlue = this.isEnoughWoodBlue();
        boolean hasEnoughWoodRed = this.isEnoughWoodRed();
        boolean hasDoubleWoodBlue = this.getWoodAmountBlue().get() >= 2 * this.enoughWoodAmount;
        boolean hasDoubleWoodRed = this.getWoodAmountRed().get() >= 2 * this.enoughWoodAmount;

        // Conditions to avoid trees
        boolean shouldAvoidTreesForGateRepair = (isBlueTeam && hasEnoughWoodBlue && isBlueGateDestroyed) ||
                (isRedTeam && hasEnoughWoodRed && isRedGateDestroyed);
        boolean shouldAvoidTreesForPrincess = isAnyPrincessCarried;
        boolean shouldAvoidTreesForExcessWood = (isBlueTeam && hasDoubleWoodBlue) || (isRedTeam && hasDoubleWoodRed);

        if (shouldAvoidTreesForGateRepair || shouldAvoidTreesForPrincess || shouldAvoidTreesForExcessWood) {
            return Collections.emptySet();
        }

        return this.getAllStructures(Tree.class).stream()
                .filter(tree -> this.isStructureInRange(agent, tree, range))
                .map(tree -> (Tree) tree) // Cast to Tree
                .collect(Collectors.toSet());
    }
    /**
     * Retrieves a set of princesses that are within a specified range of the given agent, based on team affiliation.
     *
     * @param agent The reference agent.
     * @param team The team filter ("enemy" or "ally").
     * @param range The maximum distance to check.
     * @return A set of princesses matching the criteria.
     */
    public Set<Princess> getPrincessNeighbours(Agent agent, String team, int range) {
        boolean isEnemyTeam = team.equals("enemy"); // True if searching for enemy princess
        boolean isAllyTeam = team.equals("ally");   // True if searching for ally princess

        if (isAllyTeam) {
            return this.getAllResources(Princess.class).stream()
                    .filter(princess -> princess.getTeam() == agent.getTeam())
                    .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                    .map(princess -> (Princess) princess)
                    .collect(Collectors.toSet());
        } else if (isEnemyTeam) {
            return this.getAllResources(Princess.class).stream()
                    .filter(princess -> princess.getTeam() != agent.getTeam())
                    .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                    .map(princess -> (Princess) princess)
                    .collect(Collectors.toSet());
        }

        return this.getAllResources(Princess.class).stream()
                .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                .map(princess -> (Princess) princess)
                .collect(Collectors.toSet());

    }
    /**
     * Retrieves a mapping of the positions surrounding the specified agent in relation to its current direction.
     *
     * @param agent The reference agent.
     * @return A map of surrounding positions indexed by direction.
     */
    public Map<Direction, Vector2D> getAgentSurroundingPositions(Agent agent) {
        Vector2D pos = this.getAgentPosition(agent);
        Orientation dir = this.getAgentDirection(agent);
        return Stream.of(Direction.values()).collect(Collectors.toMap(
                k -> k,
                v -> pos.afterStep(1, dir.rotate(v))
        ));
    }



    // Closest objective computation according to agent's state and AI logic
    /**
     * Calculates the Euclidean distance between two points represented by Vector2D objects.
     *
     * @param pos1 the first position
     * @param pos2 the second position
     * @return the Euclidean distance between pos1 and pos2
     */
    private double calculateDistance(Vector2D pos1, Vector2D pos2) {
        return Math.sqrt(
                Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(pos1.getY() - pos2.getY(), 2)
        );
    }
    /**
     * Finds the closest resource of a given type that satisfies a specified filter.
     *
     * @param agent the agent searching for the resource
     * @param resourceClass the class type of the resource to find
     * @param filter a predicate to filter the resources
     * @param stateName the state associated with the found resource
     * @return a Pair containing the state name and the position of the closest resource, or null if no resource is found
     */
    private synchronized Pair<String, Vector2D> findClosestResource(Agent agent, Class<? extends Resource> resourceClass, Predicate<Resource> filter, String stateName) {
        synchronized (this.map) {
            List<Cell> resources = getAllCells(
                    null,
                    null, null,
                    resourceClass, filter, // No resource filtering
                    null, null, // No agent filtering
                    true
            );


            Vector2D agentPosition = agent.getPose().getPosition();
            Vector2D closestPosition = null;
            double minDistance = Double.MAX_VALUE;

            for (Cell cell : resources) {
                double distance = calculateDistance(agentPosition, cell.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPosition = cell.getPosition();
                }
            }

            return closestPosition != null ? new Pair<>(stateName, closestPosition) : null;
        }
    }
    /**
     * Finds the closest structure of a given type that satisfies a specified filter.
     *
     * @param agent the agent searching for the structure
     * @param structureClass the class type of the structure to find
     * @param filter a predicate to filter the structures
     * @param stateName the state associated with the found structure
     * @return a Pair containing the state name and the position of the closest structure, or null if no structure is found
     */
    private synchronized Pair<String, Vector2D> findClosestStructure(Agent agent, Class<? extends MapStructure> structureClass, Predicate<MapStructure> filter, String stateName) {
        synchronized (this.map) {
            Predicate<MapStructure> combinedFilter = structure -> {
                if (structure instanceof Tree tree) {
                    return !tree.isDestroyed() && (filter == null || filter.test(structure));
                }
                return filter == null || filter.test(structure);
            };

            List<Cell> structures = getAllCells(
                    null,
                    structureClass, combinedFilter,
                    null, null, // No resource filtering
                    null, null, // No agent filtering
                    true
            );

            Vector2D agentPosition = agent.getPose().getPosition();
            Vector2D closestPosition = null;
            double minDistance = Double.MAX_VALUE;

            for (Cell cell : structures) {
                double distance = calculateDistance(agentPosition, cell.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPosition = cell.getPosition();
                }
            }

            return closestPosition != null ? new Pair<>(stateName, closestPosition) : null;
        }
    }
    /**
     * Determines the fallback action for an agent when no specific task is available.
     * This occurs when an agent's standard plan is interrupted (e.g., to protect a princess carried by a teammate),
     * making the agent to determine an appropriate action to continue his standard plan.
     *
     * @param agent the agent executing the fallback plan
     * @return a {@code Pair} containing the fallback state name (a string representing the next action to perform or location to approach)
     * and the agent's current position as a {@code Vector2D}. Returns {@code null} if no suitable
     * fallback action can be determined.
     */
    private synchronized Pair<String, Vector2D> fallbackPlanGeneral(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();

        agent.setState("fallback_general");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        // The agent is in the ally base if it's a blue agent in the blue base or a red agent in the red base

        boolean isInEnemyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.RBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.BBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() <= this.getWidth() / 2;
        // The agent is on the left side of the map


        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map


        if (isInAllyBase) {
            return new Pair<>("spawn", agent_position);
        } else if ((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed)) {
            if (isOnUpperSide) {
                return new Pair<>("land_passage_reached", agent_position);
            } else if(isOnLowerSide) {
                return new Pair<>("bridge_reached", agent_position);
            }
        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            if (isOnUpperSide) {
                return new Pair<>("towards_land_passage", agent_position);
            } else if(isOnLowerSide) {
                return new Pair<>("towards_bridge", agent_position);
            }
        } else if (isInEnemyBase) {
            return new Pair<>("enemy_gate_reached", agent_position);
        }

        return null;
    }
    /**
     * Determines the fallback action for an agent when he picks up an ally princess.
     *
     * @param agent the agent executing the fallback plan
     * @return a {@code Pair} containing the fallback state name (a string representing the next action to perform or location to approach)
     * and the agent's current position as a {@code Vector2D}. Returns {@code null} if no suitable
     * fallback action can be determined.
     */
    private synchronized Pair<String, Vector2D> fallbackPlanRescueAllyPrincess(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();
        Cell agent_cell = getCellByPosition(agent_position);

        agent.setState("fallback_ally_princess");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        boolean isInEnemyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.RBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.BBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnBridge = (agent_cell.getStructure() != null && agent_cell.getStructure() instanceof Bridge);

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() < this.getWidth() / 2;
        // The agent is on the left side of the map

        boolean isOnTheMiddle = agent_position.getX() == this.getWidth() / 2;

        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map

        if (isInAllyBase) {
            this.win = agent.getTeam(); // false blue team, true red team
            if (this.win != null) {
                String winningTeam = this.win ? "Red Team" : "Blue Team";
                System.out.println("Game Over! " + winningTeam + " wins!");

                SwingUtilities.invokeLater(() -> ConfigWindow.showGameResult(winningTeam));
            }
            return new Pair<>("game_win", agent_position);
        } else if (isInEnemyBase) {
            if (isTeamBlue) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_r"), "choose_path_back");
            } else if (isTeamRed) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_b"), "choose_path_back");
            }
        } else if (isOnTheMiddle || isOnBridge) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        } else if (((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed))) {

            if (isOnUpperSide) {
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached_back");
            } else if (isOnLowerSide) {
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached_back");
            }

        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        }

        return null;
    }
    /**
     * Determines the fallback action for an agent when he picks up an enemy princess.
     *
     * @param agent the agent executing the fallback plan
     * @return a {@code Pair} containing the fallback state name (a string representing the next action to perform or location to approach)
     * and the agent's current position as a {@code Vector2D}. Returns {@code null} if no suitable
     * fallback action can be determined.
     */
    private synchronized Pair<String, Vector2D> fallbackPlanCaptureEnemyPrincess(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();
        Cell agent_cell = getCellByPosition(agent_position);

        agent.setState("fallback_enemy_princess");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnBridge = (agent_cell.getStructure() != null && agent_cell.getStructure() instanceof Bridge);

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() <= this.getWidth() / 2;
        // The agent is on the left side of the map

        boolean isOnTheMiddle = agent_position.getX() == this.getWidth() / 2;

        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map

        if (isInAllyBase) {
            if (isTeamBlue) {
                Pair<String, Vector2D> result = findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "empty_pr", "spawn");

                if (agent_position.equals(redPrincessSpawnPoint)) {
                    if (agent.getCarriedItem() != null) {
                        agent_cell.setResource(agent.getCarriedItem());
                        agent.stopCarrying(agent.getCarriedItem());

                        synchronized (this.resourcesList) {
                            this.resourcesList.put(agent_cell.getResource().getName(), agent_cell.getResource());
                        }
                        synchronized (this.agentsList) {
                            this.agentsList.put(agent.getName(), agent);
                        }
                    }
                }
                return result;
            } else if (isTeamRed) {
                Pair<String, Vector2D> result = findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "empty_pb", "spawn");

                if (agent_position.equals(bluePrincessSpawnPoint)) {
                    if (agent.getCarriedItem() != null) {
                        agent_cell.setResource(agent.getCarriedItem());
                        agent.stopCarrying(agent.getCarriedItem());

                        synchronized (this.resourcesList) {
                            this.resourcesList.put(agent_cell.getResource().getName(), agent_cell.getResource());
                        }
                        synchronized (this.agentsList) {
                            this.agentsList.put(agent.getName(), agent);
                        }
                    }
                }
                return result;
            }
        } else if (isOnTheMiddle || isOnBridge) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        } else if ((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed)) {

            if (isOnUpperSide) {
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached_back");
            } else if (isOnLowerSide) {
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached_back");
            }

        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            if (isTeamBlue) {
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
            } else if (isTeamRed) {
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
            }
        }

        return null;
    }
    /**
     * Handles scenarios related to princesses, determining the appropriate action for the agent based on
     * princess locations, statuses (carried or not), the agent's team and position, the princess team and
     * wether the agent is carrying or not the princess.
     *
     * @param agent the agent for which to determine the princess-related action
     * @return a {@code Pair} containing the next state name and target position, or {@code null} if no
     *         princess-related action is applicable.
     */
    private synchronized Pair<String, Vector2D> handlePrincessScenarios(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();

        Optional<Princess> princessB = this.getPrincessByName("princess_b");
        Optional<Princess> princessR = this.getPrincessByName("princess_r");

        boolean isOnRightSidePB = agent_position.getX() > princessB.get().getPose().getPosition().getX();
        boolean isOnLeftSidePB = agent_position.getX() <= princessB.get().getPose().getPosition().getX();

        boolean isOnRightSidePR = agent_position.getX() > princessR.get().getPose().getPosition().getX();
        boolean isOnLeftSidePR = agent_position.getX() <= princessR.get().getPose().getPosition().getX();

        // The agent is on the left side wrt the princess
        if (princessB.isPresent() && princessR.isPresent()) {
            boolean isBlueCarried = princessB.get().isCarried();
            boolean isRedCarried = princessR.get().isCarried();

            boolean isInRedBase = getCellByPosition(agent_position).getZoneType() == Zone.RBASE;
            boolean isInBlueBase = getCellByPosition(agent_position).getZoneType() == Zone.BBASE;

            boolean isBlueOutsideRedBase = this.getCellByPosition(princessB.get().getPose().getPosition()).getZoneType() != Zone.RBASE;
            boolean isRedOutsideBlueBase = this.getCellByPosition(princessR.get().getPose().getPosition()).getZoneType() != Zone.BBASE;

            boolean isTeamBlue = !agent.getTeam();
            boolean isTeamRed = agent.getTeam();

            // AGENT CARRING PRINCESS
            // Agent is carrying ally princess
            if (agent.getCarriedItem() instanceof Princess && agent.getCarriedItem().getTeam() == agent.getTeam()) {
                return this.fallbackPlanRescueAllyPrincess(agent);
            }
            // Agent is carrying enemy princess
            if (agent.getCarriedItem() instanceof Princess && agent.getCarriedItem().getTeam() != agent.getTeam()) {
                return this.fallbackPlanCaptureEnemyPrincess(agent);
            }


            // CAPTURE ENEMY PRINCESS
            if (isTeamRed && isBlueOutsideRedBase && isInRedBase) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_r"), "capture_enemy_princess");
            } else if (isTeamBlue && isRedOutsideBlueBase && isInBlueBase) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_b"), "capture_enemy_princess");
            } else if ((isTeamRed && isBlueOutsideRedBase) || (isTeamBlue && isRedOutsideBlueBase)) {
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() != agent.getTeam(),
                        "capture_enemy_princess");
            }

            // RESCUE ALLY PRINCESS
            // Ally princess is being carried by teammate and the agent is in enemies base
            // We avoid to check if agent is in enemy base and enemy princess as well because it would mean the game is over
            if ((isBlueCarried && isTeamBlue && isInRedBase)) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_r"), "rescue_ally_princess");
            } else if ((isRedCarried && isTeamRed && isInBlueBase)) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_b"), "rescue_ally_princess");
            }

            // Ally princess is outside enemy base, either carried or dropped, so stay behind the princess
            if ((isTeamBlue && isOnRightSidePB && isBlueOutsideRedBase) || (isTeamRed && isOnLeftSidePR && isRedOutsideBlueBase)) {
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(),
                        "rescue_ally_princess");
            }

        }

        return null;
    }
    /**
     * Handles scenarios related to gates, determining the appropriate action for the Gatherer agent, primarily
     * focused on repairing destroyed gates if the agent's team has enough wood.
     *
     * @param agent the agent for which to determine the gate-related action
     * @return a {@code Pair} containing the next state name and target position for gate repair, or
     *         {@code null} if no gate-related action is applicable (e.g., no destroyed gates or not enough wood).
     */
    private synchronized Pair<String, Vector2D> handleGatesScenarios(Agent agent) {
        Optional<Gate> gate_b1 = this.getGateByName("gate_b1");
        Optional<Gate> gate_b2 = this.getGateByName("gate_b2");
        Optional<Gate> gate_r1 = this.getGateByName("gate_r1");
        Optional<Gate> gate_r2 = this.getGateByName("gate_r2");

        if (gate_b1.isPresent() && gate_b2.isPresent() && gate_r1.isPresent() && gate_r2.isPresent()
                && !(agent.getCarriedItem() instanceof Princess)) {

            boolean isTeamBlue = !agent.getTeam();
            boolean isTeamRed = agent.getTeam();

            boolean isGateB1Destroyed = gate_b1.get().isDestroyed();
            boolean isGateB2Destroyed = gate_b2.get().isDestroyed();
            boolean isBlueGateDestroyed = isGateB1Destroyed || isGateB2Destroyed;

            boolean isGateR1Destroyed = gate_r1.get().isDestroyed();
            boolean isGateR2Destroyed = gate_r2.get().isDestroyed();
            boolean isRedGateDestroyed = isGateR1Destroyed || isGateR2Destroyed;



            if (isTeamBlue) {
                synchronized (this.getWoodAmountBlue()) {
                    if (isBlueGateDestroyed && this.getWoodAmountBlue().get() >= this.enoughWoodAmount) {
                        if (isGateB1Destroyed) {
                            Vector2D gate_b1_pos = gate_b1.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_b1_pos.getX() + 1, gate_b1_pos.getY() - 1));
                        } else if (isGateB2Destroyed) {
                            Vector2D gate_b2_pos = gate_b2.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_b2_pos.getX() + 1, gate_b2_pos.getY() + 1));
                        }
                    }
                }
            }

            if (isTeamRed) {
                synchronized (this.getWoodAmountRed()) {
                    if (isRedGateDestroyed && this.getWoodAmountRed().get() >= this.enoughWoodAmount) {
                        if (isGateR1Destroyed) {
                            Vector2D gate_r1_pos = gate_r1.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_r1_pos.getX() - 1, gate_r1_pos.getY() - 1));
                        } else if (isGateR2Destroyed) {
                            Vector2D gate_r2_pos = gate_r2.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_r2_pos.getX() - 1, gate_r2_pos.getY() + 1));
                        }
                    }
                }
            }
        }

        return null;
    }
    /**
     * Determines the closest objective for a soldier agent based on its current state and the game's
     * overall state (including princess scenarios).  This method implements the core soldier AI logic,
     * defining the sequence of actions from spawning to capturing the enemy princess or rescuing the
     * ally princess. It also handles game over conditions.
     *
     * @param agent the soldier agent for which to determine the closest objective
     * @return a {@code Pair} containing the next state name and target position, or {@code null} if no
     *         objective can be determined (e.g., invalid state).
     */
    public synchronized Pair<String, Vector2D> getClosestObjectiveSoldier(Agent agent) {

        if (this.win != null) {
            if (agent.getTeam() == this.win) {
                return new Pair<>("my_team_won", agent.getPose().getPosition());
            } else {
                return new Pair<>("my_team_lost", agent.getPose().getPosition());
            }
        }

        Pair<String, Vector2D> princessScenario = handlePrincessScenarios(agent);
        if (princessScenario != null) {
            return princessScenario;
        }

        switch (agent.getState()) {
            case "spawn":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "exit_from_ally_base");

            case "exit_from_ally_base":
                if (!agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_b"), "choose_path");
                } else if (agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_r"), "choose_path");
                }

            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge", agent.getPose().getPosition());
                }

            case "towards_land_passage":
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached");

            case "towards_bridge":
                Cell cell = this.getCellByPosition(agent.getPose().getPosition());
                boolean isOnBridge = cell.getStructure() != null && (cell.getStructure() instanceof Bridge);

                if (isOnBridge) {
                    return new Pair<>("bridge_reached", agent.getPose().getPosition());
                }
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached");

            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached");

            case "enemy_gate_reached":
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(), "ally_princess_reached");

            case "rescue_ally_princess":
                if (this.win == null) {
                    //I was an agent following my princess while being rescued and the princess fell of one of my teammates

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                }

            case "capture_enemy_princess":
                if (this.win == null) {
                    //I was an agent following enemy princess while being rescued and the princess fell of one of my enemies

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following enemy princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                }

            default:
                return new Pair<>("dead", agent.getPose().getPosition());

        }
    }
    /**
     * Determines the closest objective for a gatherer agent based on its current state and the game's
     * overall state (including princess and gate scenarios). This method implements the core gatherer
     * AI logic, defining the sequence of actions from spawning to gathering wood, repairing gates, and
     * eventually transitioning to soldier behavior. It also handles game over conditions.
     *
     * @param agent the gatherer agent for which to determine the closest objective
     * @return a {@code Pair} containing the next state name and target position, or {@code null} if no
     *         objective can be determined (e.g., invalid state).
     */
    public synchronized Pair<String, Vector2D> getClosestObjectiveGatherer(Agent agent) {

        if (this.win != null) {
            if (agent.getTeam() == this.win) {
                return new Pair<>("my_team_won", agent.getPose().getPosition());
            } else {
                return new Pair<>("my_team_lost", agent.getPose().getPosition());
            }
        }

        Pair<String, Vector2D> princessScenario = handlePrincessScenarios(agent);
        if (princessScenario != null) {
            return princessScenario;
        }

        Pair<String, Vector2D> gatesScenario = handleGatesScenarios(agent);
        if (gatesScenario != null) {
            return gatesScenario;
        }

        switch (agent.getState()) {
            case "spawn", "repairing_gate":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "exit_from_ally_base");

            case "exit_from_ally_base":
                if (!agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_b"), "gather_wood");
                } else if (agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_r"), "gather_wood");
                }

            case "gather_wood":

                if (!agent.getTeam()) {
                    synchronized (this.getWoodAmountBlue()) { // Maybe check if the princess is carried?
                        if (!agent.getTeam() && this.getWoodAmountBlue().get() >= (this.enoughWoodAmount * 2)) { // If agent is blue and there is enough wood
                            return new Pair("choose_path", agent.getPose().getPosition());
                        }
                    }
                }

                if (agent.getTeam()) {
                    synchronized (this.getWoodAmountRed()) {
                        if (this.getWoodAmountRed().get() >= (this.enoughWoodAmount * 2)) { // Else if agent is red and there is enough wood
                            return new Pair("choose_path", agent.getPose().getPosition());
                        }
                    }
                }

                return findClosestStructure(agent, Tree.class, null, "tree_reached");

            case "tree_reached":
                return new Pair("gather_wood", agent.getPose().getPosition());

            case "repair_destroyed_gate":
                return new Pair("gather_wood", agent.getPose().getPosition());

            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge", agent.getPose().getPosition());
                }

            case "towards_land_passage":
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached");

            case "towards_bridge":
                Cell cell = this.getCellByPosition(agent.getPose().getPosition());
                boolean isOnBridge = cell.getStructure() != null && (cell.getStructure() instanceof Bridge);

                if (isOnBridge) {
                    return new Pair<>("bridge_reached", agent.getPose().getPosition());
                }
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached");

            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached");

            case "enemy_gate_reached":
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(), "ally_princess_reached");

            case "rescue_ally_princess":
                if (this.win == null) {
                    //I was an agent following my princess while being rescued and the princess fell of one of my teammates

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                }

            case "capture_enemy_princess":
                if (this.win == null) {
                    //I was an agent following enemy princess while being rescued and the princess fell of one of my enemies

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following enemy princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                }

            default:
                return new Pair<>("dead", agent.getPose().getPosition());
            }
    }
    /**
     * Determines the closest objective for the given agent, dispatching to the appropriate handler
     * method based on the agent's type (soldier or gatherer).
     *
     * @param agent the agent for which to determine the closest objective
     * @return a {@code Pair} containing the next state name and target position, or {@code null} if the
     *         agent type is not recognized.
     */
    public synchronized Pair<String, Vector2D> getClosestObjective(Agent agent) {
        if (!(agent instanceof Gatherer)) {
            return getClosestObjectiveSoldier(agent);
        } else if (agent instanceof Gatherer) {
            return getClosestObjectiveGatherer(agent);
        }

        return null; // Return null if the agent type is not recognized
    }




    // Artifacts management
    /**
     * Spawns a princess for the specified team in their opponent's base.  The princess is placed
     * randomly within the column closest to the center of the opponent's base.
     *
     * @param team {@code true} to spawn the red princess in the blue base, {@code false} to spawn
     *             the blue princess in the red base.
     * @throws IllegalStateException if there are no available cells in the opponent's base to
     *                               spawn the princess.
     */
    private void spawnPrincess(boolean team) {
        synchronized (this.map) {
            synchronized (this.structuresList) {
                Zone opponentBaseZone = team ? Zone.BBASE : Zone.RBASE;

                // Get all cells in the opponent's base zone
                List<Cell> allCellsInBaseZone = getAllCells(
                        opponentBaseZone,
                        null, null,
                        null, null,
                        null, null,
                        true
                );

                if (allCellsInBaseZone.isEmpty()) {
                    throw new IllegalStateException("No available cells in the opponent's base zone to spawn the Princess.");
                }

                // Determine the target column for spawning
                int targetColumn = team ? 0 : this.getWidth() - 1; // First column for BBASE, last column for RBASE

                // Filter cells to only those in the designated column
                List<Cell> availableCells = allCellsInBaseZone.stream()
                        .filter(cell -> cell.getX() == targetColumn)
                        .toList();

                if (availableCells.isEmpty()) {
                    throw new IllegalStateException("No available cells in the designated column to spawn the Princess.");
                }

                // Randomly select a cell and spawn the princess
                Cell randomCell = availableCells.get(RAND.nextInt(availableCells.size()));
                Vector2D princessSpawnPoint = randomCell.getPosition();
                String name = team ? "princess_r" : "princess_b";
                if (name == "princess_r") {
                    Empty empty_pr = new Empty(new Pose(princessSpawnPoint, Orientation.SOUTH), "empty_pr");
                    this.getCellByPosition(princessSpawnPoint).setStructure(empty_pr);
                    structuresList.put("empty_pr", empty_pr);

                    redPrincessSpawnPoint = princessSpawnPoint;
                } else if (name == "princess_b" ) {
                    Empty empty_pb = new Empty(new Pose(princessSpawnPoint, Orientation.SOUTH), "empty_pb");
                    this.getCellByPosition(princessSpawnPoint).setStructure(empty_pb);
                    structuresList.put("empty_pb", empty_pb);

                    bluePrincessSpawnPoint = princessSpawnPoint;

                }
                Princess princess = new Princess(
                        name,
                        team,
                        new Pose(princessSpawnPoint, Orientation.SOUTH)
                );

                randomCell.setResource(princess);
                resourcesList.put(princess.getName(), princess);
                System.out.println("Princess spawned at: " + randomCell.getX() + ", " + randomCell.getY());
            }
        }
    }
    /**
     * Retrieves all structures of a specific class from the structures list.
     *
     * @param structureClass the class of the structures to retrieve (e.g., {@code Gate.class}).
     * @return a {@code Set} containing all structures of the specified class on the map.
     */
    private synchronized Set<MapStructure> getAllStructures(Class<? extends MapStructure> structureClass) {
        synchronized (this.structuresList) {
            return this.structuresList.values()
                    .stream()
                    .filter(structure -> structureClass.isInstance(structure))
                    .collect(Collectors.toSet());
        }
    }
    /**
     * Retrieves all resources of a specific class from the resources list.
     *
     * @param resourceClass the class of the resources to retrieve (e.g., {@code Princess.class}).
     * @return a {@code Set} containing all resources of the specified class on the map.
     */
    private synchronized Set<Resource> getAllResources(Class<? extends Resource> resourceClass) {
        synchronized (this.resourcesList) {
            return this.resourcesList.values()
                    .stream()
                    .filter(resource -> resourceClass.isInstance(resource))
                    .collect(Collectors.toSet());
        }
    }
    /**
     * Checks if a specific structure exists on the structures list.
     *
     * @param structure the structure to check for existence.
     * @return {@code true} if the structure exists on the map, {@code false} otherwise.
     */
    private synchronized boolean containsStructure(MapStructure structure) {
        synchronized (this.structuresList) {
            return this.structuresList.containsKey(structure.getName());
        }
    }
    /**
     * Checks if a specific resource exists on the resources list.
     *
     * @param resource the resource to check for existence.
     * @return {@code true} if the resource exists on the map, {@code false} otherwise.
     */
    private synchronized boolean containsResource(Resource resource) {
        synchronized (this.resourcesList) {
            return this.resourcesList.containsKey(resource.getName());
        }
    }
    /**
     * Checks if a structure is within a specified range of an agent.
     *
     * @param agent     the agent to check the distance from.
     * @param structure the structure to check the distance to.
     * @param range     the maximum range within which the structure must be.
     * @return {@code true} if the structure is within the specified range of the agent,
     *         {@code false} otherwise.
     */
    private boolean isStructureInRange(Agent agent, MapStructure structure, int range) {
        if (!containsAgent(agent) || !containsStructure(structure)) {
            return false;
        }
        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = structure.getPose().getPosition();
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }
    /**
     * Checks if a resource is within a specified range of an agent.
     *
     * @param agent     the agent to check the distance from.
     * @param resource  the resource to check the distance to.
     * @param range     the maximum range within which the resource must be.
     * @return {@code true} if the resource is within the specified range of the agent and is not
     *         carried, {@code false} otherwise.
     */
    private boolean isResourceInRange(Agent agent, Resource resource, int range) {
        if (!containsAgent(agent) || !containsResource(resource) || resource.isCarried()) {
            return false;
        }
        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = resource.getPose().getPosition();
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }
    /**
     * Gets the current amount of wood held by the red team.
     *
     * @return an {@code AtomicInteger} representing the red team's wood amount.
     */
    public synchronized AtomicInteger getWoodAmountRed() {
        return this.woodAmountRed;
    }
    /**
     * Gets the current amount of wood held by the blue team.
     *
     * @return an {@code AtomicInteger} representing the blue team's wood amount.
     */
    public synchronized AtomicInteger getWoodAmountBlue() {
        return this.woodAmountBlue;
    }
    /**
     * Checks if the red team has enough wood (greater than or equal to the required amount).
     *
     * @return {@code true} if the red team has enough wood, {@code false} otherwise.
     */
    private synchronized boolean isEnoughWoodRed() { return (this.getWoodAmountRed().get() >= this.enoughWoodAmount); }
    /**
     * Checks if the blue team has enough wood (greater than or equal to the required amount).
     *
     * @return {@code true} if the blue team has enough wood, {@code false} otherwise.
     */
    private synchronized boolean isEnoughWoodBlue() { return (this.getWoodAmountBlue().get() >= this.enoughWoodAmount); }
    /**
     * Adds wood to the appropriate team's wood count based on the agent's team.
     *
     * @param agent the agent that gathered the wood.
     */
    private synchronized void addWood(Agent agent) {
        if (!agent.getTeam()) {
            synchronized (this.woodAmountBlue) {
                woodAmountBlue.incrementAndGet();
            }
        } else if (agent.getTeam()) {
            synchronized (this.woodAmountRed) {
                woodAmountRed.incrementAndGet();
            }
        }
    }
    /**
     * Retrieves a gate by its name.
     *
     * @param gName the name of the gate to retrieve.
     * @return an {@code Optional} containing the gate if found, or an empty {@code Optional} if not.
     */
    public synchronized Optional<Gate> getGateByName(String gName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(entry -> gName.equals(entry.getName()))
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .findFirst();
        }
    }
    /**
     * Retrieves a tree by its name.
     *
     * @param tName the name of the tree to retrieve.
     * @return an {@code Optional} containing the tree if found, or an empty {@code Optional} if not.
     */
    public synchronized Optional<Tree> getTreeByName(String tName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Tree.class).stream()
                    .filter(entry -> tName.equals(entry.getName()))
                    .map(tree -> (Tree) tree) // Cast to Tree
                    .findFirst();
        }
    }
    /**
     * Retrieves a princess by her name.
     *
     * @param pName the name of the princess to retrieve.
     * @return an {@code Optional} containing the princess if found, or an empty {@code Optional} if not.
     */
    public synchronized Optional<Princess> getPrincessByName(String pName) {
        synchronized (this.resourcesList) {
            return this.getAllResources(Princess.class).stream()
                    .filter(entry -> pName.equals(entry.getName()))
                    .map(princess -> (Princess) princess) // Cast to Princess
                    .findFirst();
        }
    }


    // Map
    /**
     * Returns a list of cells based on filtering criteria.  This method allows for flexible
     * filtering of cells based on their zone type, contained structures, contained resources,
     * and contained agents.  Predicates can be used to further refine the filtering based on
     * specific properties of these elements.
     *
     * @param zoneType           Optional zone type to filter by (can be {@code null} for no zone
     *                             filtering).
     * @param structureClass     Optional class type of structure to filter by (can be {@code null}
     *                             for no structure filtering).
     * @param structurePredicate Optional predicate to filter structures by specific fields (can
     *                             be {@code null} for no structure predicate filtering).
     * @param resourceClass      Optional class type of resource to filter by (can be {@code null}
     *                             for no resource filtering).
     * @param resourcePredicate  Optional predicate to filter resources by specific fields (can
     *                             be {@code null} for no resource predicate filtering).
     * @param agentClass         Optional class type of agent to filter by (can be {@code null}
     *                             for no agent filtering).
     * @param agentPredicate     Optional predicate to filter agents by specific fields (can be
     *                             be {@code null} for no agent predicate filtering).
     * @param includeMatching    If {@code true}, returns cells *matching* the criteria; if
     *                             {@code false}, returns the *complement* (cells *not* matching).
     * @return A {@code List} of cells filtered according to the specified criteria, ordered in
     *         raster scan order (row by row, left to right).
     */
    public synchronized List<Cell> getAllCells(
            Zone zoneType,
            Class<? extends MapStructure> structureClass, Predicate<MapStructure> structurePredicate,
            Class<? extends Resource> resourceClass, Predicate<Resource> resourcePredicate,
            Class<? extends Agent> agentClass, Predicate<Agent> agentPredicate,
            boolean includeMatching) {

        synchronized (this.map) {
            return Arrays.stream(map)
                    .flatMap(Arrays::stream)
                    .filter(cell -> {
                        boolean matches = true;

                        if (zoneType != null) {
                            matches &= zoneType.equals(cell.getZoneType());
                        }
                        if (structureClass != null) {
                            matches &= structureClass.isInstance(cell.getStructure()) &&
                                    (structurePredicate == null || structurePredicate.test(cell.getStructure()));
                        }

                        if (resourceClass != null) {
                            matches &= resourceClass.isInstance(cell.getResource()) &&
                                    (resourcePredicate == null || resourcePredicate.test(cell.getResource()));
                        }
                        if (agentClass != null && cell.getAgent() != null) {
                            matches &= agentClass.isInstance(cell.getAgent()) &&
                                    (agentPredicate == null || agentPredicate.test(cell.getAgent()));
                        }

                        return includeMatching == matches;
                    })
                    .collect(Collectors.toList());
        }
    }
    /**
     * Retrieves the cell at the specified coordinates.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @return the {@code Cell} at the specified coordinates, or {@code null} if the coordinates are
     *         outside the map boundaries.
     */
    public synchronized Cell getCellByPosition(int x, int y) {
        synchronized (this.map) {
            if (!this.isPositionInside(x, y)) {
                return null;
            }
            return map[x][y];
        }
    }
    /**
     * Retrieves the cell at the specified position.
     *
     * @param position the {@code Vector2D} representing the position of the cell.
     * @return the {@code Cell} at the specified position, or {@code null} if the position is
     *         outside the map boundaries.
     */
    public synchronized Cell getCellByPosition(Vector2D position) {
        return this.getCellByPosition(position.getX(), position.getY());
    }
    /**
     * Retrieves a random unoccupied cell within the specified zone.  Optionally, the cell can be
     * restricted to one of the four corners of the zone.
     *
     * @param agent the agent for which to check for cell occupation.
     * @param zone  the zone in which to find a random cell.
     * @param corner if {@code true}, restricts the selection to corner cells; if {@code false},
     *               allows selection from any cell within the zone.
     * @return a random unoccupied {@code Cell} within the specified zone, or {@code null} if no
     *         such cell exists.
     */
    private synchronized Cell getRandomCell(Agent agent, Zone zone, boolean corner) {
        // Get all unoccupied cells in the specified zone
        List<Cell> availableCells = getAllCells(
                zone,
                null, null,
                null, null,
                null, null,
                true
        );

        if (availableCells.isEmpty()) {
            return null; // No available cells in the base
        }

        // If corner selection is required, filter the list
        if (corner) {
            availableCells = filterCornerCells(availableCells, zone);
        }

        if (availableCells.isEmpty()) {
            return null; // No available corner cells
        }

        // Select a random cell from the available ones
        Cell randomCell = availableCells.get(RAND.nextInt(availableCells.size()));
        while (randomCell.isOccupied(agent, null)) {
            randomCell = availableCells.get(RAND.nextInt(availableCells.size()));
        }

        return randomCell;
    }
    /**
     * Filters a list of cells to only include those located in the corners of a specified zone.
     * The specific corners included depend on the zone type.
     *
     * @param cells the list of cells to filter.
     * @param zone  the zone to consider when determining corners.
     * @return a new {@code List} containing only the corner cells, or the original list if it is empty.
     */
    private List<Cell> filterCornerCells(List<Cell> cells, Zone zone) {
        if (cells.isEmpty()) return cells;

        // Find min/max x and y values
        final int minX = cells.stream().mapToInt(c -> c.getPosition().getX()).min().orElse(Integer.MAX_VALUE);
        final int maxX = cells.stream().mapToInt(c -> c.getPosition().getX()).max().orElse(Integer.MIN_VALUE);
        final int minY = cells.stream().mapToInt(c -> c.getPosition().getY()).min().orElse(Integer.MAX_VALUE);
        final int maxY = cells.stream().mapToInt(c -> c.getPosition().getY()).max().orElse(Integer.MIN_VALUE);

        // Filter only the four corner cells
        if (zone == Zone.BBASE) {

            return cells.stream()
                    .filter(cell -> {
                        int x = cell.getPosition().getX();
                        int y = cell.getPosition().getY();
                        return (x == minX && y == minY) || // Bottom-left
                                (x == minX && y == maxY) || // Top-left
                                (x == maxX - 1 && y == minY) || // Bottom-right
                                (x == maxX - 1 && y == maxY);   // Top-right
                    })
                    .collect(Collectors.toList());

        } else if (zone == Zone.RBASE) {

            return cells.stream()
                    .filter(cell -> {
                        int x = cell.getPosition().getX();
                        int y = cell.getPosition().getY();
                        return (x == minX + 1 && y == minY) || // Bottom-left
                                (x == minX + 1 && y == maxY) || // Top-left
                                (x == maxX  && y == minY) || // Bottom-right
                                (x == maxX  && y == maxY);   // Top-right
                    })
                    .collect(Collectors.toList());

        } else {

            return cells.stream()
                    .filter(cell -> {
                        int x = cell.getPosition().getX();
                        int y = cell.getPosition().getY();
                        return (x == minX && y == minY) || // Bottom-left
                                (x == minX  && y == maxY) || // Top-left
                                (x == maxX  && y == minY) || // Bottom-right
                                (x == maxX  && y == maxY);   // Top-right
                    })
                    .collect(Collectors.toList());


        }

    }

    /**
     * Checks if a given position is within the boundaries of the map.
     *
     * @param x the x-coordinate of the position.
     * @param y the y-coordinate of the position.
     * @return {@code true} if the position is inside the map, {@code false} otherwise.
     */
    public boolean isPositionInside(int x, int y) {
        return ((x >= 0 && x < this.width && y >= 0 && y < this.height));
    }
    /**
     * Sets the {@code MapView} associated with this map.
     *
     * @param view the {@code MapView} to set.
     */
    public void setView(MapView view) {
        this.view = view;
    }

    // Map Construction
    /**
     * Creates the different zone types on the map (bases, river, battlefield).
     */
    public void createZones() {
        createBaseZones();
        createRiver();
        createBattlefield();
    }
    /**
     * Adds structures (walls, gates, bridge, trees) to the map.
     */
    public void addStructures() {
        addWallsAndGates();
        addBridge();
        addTrees();
    }
    /**
     * Adds resources (princesses) to the map.
     */
    public void addResources() {
        spawnPrincess(false);
        spawnPrincess(true);
    }
    /**
     * Creates the base zones for both teams.
     */
    private void createBaseZones() {
        // Set base zones
        for (int x = 0; x < baseWidth; x++) {
            for (int y = baseHeight; y < this.getHeight() - baseHeight; y++) {
                map[x][y] = new Cell(Zone.BBASE, x, y);
                map[this.getWidth() - x - 1][this.getHeight() - y - 1] = new Cell(Zone.RBASE, this.getWidth() - x - 1, this.getHeight() - y - 1);
            }
        }

        // Set OUT_OF_MAP zones above and below the bases

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = 0; y < baseHeight - 1; y++) {
                map[x][y] = new Cell(Zone.OUT_OF_MAP, x, y);
                map[x][this.getHeight() - y - 1] = new Cell(Zone.OUT_OF_MAP, x, this.getHeight() - y - 1);

                int mirroredX = this.getWidth() - x - 1;
                map[mirroredX][y] = new Cell(Zone.OUT_OF_MAP, mirroredX, y);
                map[mirroredX][this.getHeight() - y - 1] = new Cell(Zone.OUT_OF_MAP, mirroredX, this.getHeight() - y - 1);
            }
        }

    }
    /**
     * Creates the river zone in the middle of the map.
     */
    private void createRiver() {
        int centerX = this.getWidth() / 2;

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                map[x][y] = new Cell(Zone.OUT_OF_MAP, x, y);
            }
        }

    }
    /**
     * Creates the battlefield zone, including specific Empty structure locations.
     */
    private void createBattlefield() {
        int crossableY1 = (this.getHeight() / 3);
        int crossableY2 = crossableY1 - 1;
        int crossableY3 = crossableY1 - 2;
        int middleX = this.getWidth() / 2;

        boolean flagPlaced = false;

        for (int x = 0; x < this.getWidth(); x++) {
            if (x < baseWidth || x >= this.getWidth() - baseWidth) {
                continue;
            }

            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x, crossableY2);
            }
            if (map[x][crossableY3] != null && map[x][crossableY3].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY3] = new Cell(Zone.BATTLEFIELD, x, crossableY3);
            }
        }

        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
                }
            }


            if (!flagPlaced && x == middleX) {
                if (map[x][crossableY1] != null) {
                    Empty empty1 = new Empty(new Pose(new Vector2D(x, crossableY1), Orientation.SOUTH), "half");
                    map[x][crossableY1].setStructure(empty1);
                    structuresList.put("empty_" + x + "_" + crossableY1, empty1);
                }
                if (map[x][crossableY2] != null) {
                    Empty empty2 = new Empty(new Pose(new Vector2D(x, crossableY2), Orientation.SOUTH), "half");
                    map[x][crossableY2].setStructure(empty2);
                    structuresList.put("empty_" + x + "_" + crossableY2, empty2);
                }
                if (map[x][crossableY3] != null) {
                    Empty empty3 = new Empty(new Pose(new Vector2D(x, crossableY3), Orientation.SOUTH), "half");
                    map[x][crossableY3].setStructure(empty3);
                    structuresList.put("empty_" + x + "_" + crossableY3, empty3);
                }
                flagPlaced = true;
            }
        }
    }
    /**
     * Adds walls and gates to the bases.
     */
    private void addWallsAndGates() {
        int blueWallsIdx = 1;
        int redWallsIdx = 1;

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate1 = new Gate("gate_b1", 50, false, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_b2", 50, false, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);
                        map[x][y].setZoneType(Zone.BBASE);
                        map[x][y + 1].setZoneType(Zone.BBASE);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);

                        Empty empty1 = new Empty(new Pose(new Vector2D(x + 2, y), Orientation.SOUTH), "base_b");
                        Empty empty2 = new Empty(new Pose(new Vector2D(x + 2, y + 1), Orientation.SOUTH), "base_b");
                        map[x+2][y].setStructure(empty1);
                        map[x+2][y + 2].setStructure(empty2);
                        structuresList.put("empty_" + (x + 2) + "_" + y, empty1);
                        structuresList.put("empty_" + (x + 2) + "_" + (y + 1), empty2);
                    } else if (y != this.getHeight() / 2) {
                        Wall wall = new Wall("wall_b" + blueWallsIdx, false, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        map[x][y].setStructure(wall);
                        structuresList.put(wall.getName(), wall);
                        blueWallsIdx++;
                    }
                }
            }
        }

        for (int x = this.getWidth() - baseWidth - 1; x < this.getWidth(); x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate1 = new Gate("gate_r1", 50, true, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_r2", 50, true, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);

                        map[x][y].setZoneType(Zone.RBASE);
                        map[x][y + 1].setZoneType(Zone.RBASE);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);

                        Empty empty1 = new Empty(new Pose(new Vector2D(x - 2, y), Orientation.SOUTH), "base_r");
                        Empty empty2 = new Empty(new Pose(new Vector2D(x - 2, y + 1), Orientation.SOUTH), "base_r");
                        map[x-2][y].setStructure(empty1);
                        map[x-2][y + 2].setStructure(empty2);
                        structuresList.put("empty_" + (x - 2) + "_" + y, empty1);
                        structuresList.put("empty_" + (x - 2) + "_" + (y + 1), empty2);
                    } else if (y != this.getHeight() / 2) {
                        Wall wall = new Wall("wall_r" + redWallsIdx, true, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        map[x][y].setStructure(wall);
                        structuresList.put(wall.getName(), wall);
                        redWallsIdx++;
                    }
                }
            }
        }
    }
    /**
     * Adds the bridge structure to the map.
     */
    private void addBridge() {
        int bridgeY = (2 * this.getHeight()) / 3 + 1;
        int bridgeXStart = this.getWidth() / 2 - 1;
        int bridgeXEnd = this.getWidth() / 2 + 1;

        for (int x = bridgeXStart; x <= bridgeXEnd; x++) {
            for (int y = bridgeY; y < bridgeY + 2; y++) {
                Bridge bridge = new Bridge(10, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                map[x][y].setStructure(bridge);
                map[x][y].setZoneType(Zone.BATTLEFIELD);
                structuresList.put("bridge_" + x + "_" + y, bridge);
            }
        }
    }
    /**
     * Checks if a given cell is a valid location for a tree.  A valid tree cell must be
     * unoccupied, within the battlefield zone, and not adjacent to another tree.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @return {@code true} if the cell is a valid tree location, {@code false} otherwise.
     */
    private boolean isValidTreeCell(int x, int y) {
        Cell cell = this.getCellByPosition(x, y);
        return cell != null
                && cell.getStructure() == null
                && cell.getZoneType() != Zone.BBASE
                && cell.getZoneType() != Zone.RBASE
                && cell.getZoneType() != Zone.OUT_OF_MAP
                && cell.getZoneType() == Zone.BATTLEFIELD;
    }
    /**
     * Removes cells adjacent to a given tree position from a list of available positions.  This
     * prevents trees from being placed too close to each other.
     *
     * @param positions the list of available positions.
     * @param treePos   the position of the tree.
     */
    private void removeAdjacentCells(List<Vector2D> positions, Vector2D treePos) {
        int[][] neighbors = {
                {-1, -1}, {0, -1}, {1, -1},  // Top-left, Top, Top-right
                {-1,  0},         {1,  0},  // Left,        Right
                {-1,  1}, {0,  1}, {1,  1}   // Bottom-left, Bottom, Bottom-right
        };

        positions.remove(treePos);  // Remove the tree position itself

        for (int[] offset : neighbors) {
            Vector2D neighborPos = Vector2D.of(treePos.getX() + offset[0], treePos.getY() + offset[1]);
            positions.remove(neighborPos); // Remove adjacent positions
        }
    }
    /**
     * Adds trees to the map, distributing them randomly within designated spawn areas.  Trees are
     * placed with a minimum distance between them.
     */
    private void addTrees() {
        int treeCount = (this.width * this.height) / 33;

        // Define spawn area dimensions
        int northSpawnAreaWidth = this.getWidth() / 2 - 1;
        int southSpawnAreaWidth = this.getWidth() / 2 - 1;
        int spawnAreaHeight = this.getHeight() / 4 - 1;

        // Define bounds for the four areas (northern and southern)
        int northStartY = 0;
        int southStartY = this.getHeight() - spawnAreaHeight;
        int area1StartX = 0;
        int area2StartX = this.getWidth() - northSpawnAreaWidth;
        int area3StartX = 0;
        int area4StartX = this.getWidth() - southSpawnAreaWidth;

        // Define a list of all spawnable cells
        List<Vector2D> spawnablePositions = new ArrayList<>();

        // Add northern areas to spawnable cells
        for (int x = area1StartX; x < area1StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }
        for (int x = area2StartX; x < area2StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }

        // Add southern areas to spawnable cells
        for (int x = area3StartX; x < area3StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }
        for (int x = area4StartX; x < area4StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }

        // Randomly place trees in the spawnable cells
        Random random = new Random();
        for (int i = 1; i <= treeCount && !spawnablePositions.isEmpty(); i++) {
            int index = random.nextInt(spawnablePositions.size());
            Vector2D selectedPos = spawnablePositions.remove(index);  // Remove chosen cell from list
            Cell selectedCell = map[selectedPos.getX()][selectedPos.getY()];

            // Place tree
            Tree tree = new Tree("tree_" + i, 50, 30000, new Pose(selectedPos, Orientation.SOUTH));
            selectedCell.setStructure(tree);
            structuresList.put(tree.getName(), tree);

            removeAdjacentCells(spawnablePositions, selectedPos);
        }
    }

}
