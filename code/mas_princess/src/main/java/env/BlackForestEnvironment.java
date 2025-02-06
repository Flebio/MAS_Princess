package env;

import env.utils.*;
import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static env.utils.AbsoluteMovement.*;


/**
 * The `BlackForestEnvironment` class extends the `Environment` class and implements `MapEnvironment`.
 * It provides an interface between the AgentSpeak agents and the simulation world.
 *
 * This class is responsible for handling percepts, actions, and interactions between agents
 * and the game environment. It ensures that multi-agent behaviors are synchronized with
 * the simulation model (`BlackForestModel`).
 */
public class BlackForestEnvironment extends Environment implements MapEnvironment {

    private static final Random RAND = new Random();
    static Logger logger = Logger.getLogger(BlackForestEnvironment.class.getName());
    private MapModel model;
    private BlackForestView view;
    private long threadSleep, threadSleepRespawn;


    /**
     * Initializes the BlackForestEnvironment with the given parameters.
     * Sets up the model, view, and defines sleep intervals for agent actions.
     *
     * @param args Array containing width and height of the environment.
     */
    @Override
    public void init(final String[] args) {
        this.model = new BlackForestModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]), null);
        this.threadSleep = 1000L / this.model.getFPS(); // 1000ms / 4 = 250ms = 0.25s
        this.threadSleepRespawn = threadSleep * 20; // 250ms * 60 = 15000ms = 15s
        this.view = new BlackForestView(model);

        this.model.setView(this.view);

        view.setVisible(true);
    }

    /**
     * Notifies the view that the model has changed.
     * Used to refresh the GUI representation of the environment.
     */
    @Override
    public void notifyModelChangedToView() {
        view.notifyModelChanged();
    }

    /**
     * Initializes an agent if it does not already exist in the model.
     * If the agent exists, returns the existing agent. Otherwise, creates a new instance based on the agent's name pattern.
     *
     * @param agentName The name of the agent to initialize.
     * @return The initialized agent or null if initialization fails.
     */
    @Override
    public Agent initializeAgentIfNeeded(String agentName) {
        Optional<Agent> optionalAgent = this.model.getAgentByName(agentName);

        if (optionalAgent.isEmpty()) {
            String[] parts = agentName.split("_");

            String type = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);  // Type
            String team = parts[1];  // Team (after underscore)

            try {
                Class<?> agentClass = Class.forName("env.agents." + type);

                Constructor<?> constructor = agentClass.getConstructor(String.class, boolean.class);

                Agent agent = (Agent) constructor.newInstance(agentName, team.contains("r"));  // 'r' -> true, 'b' -> false

                this.model.spawnAgent(agent);
                notifyModelChangedToView();

                logger.info("New " + (team.contains("r") ? "red" : "blue") + " " + agent.getClass().getSimpleName().toLowerCase() + " spawned with pose " + agent.getPose());

                return agent;
            } catch (ClassNotFoundException e) {
                logger.warning("Class not found: " + type);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            return optionalAgent.get();
        }

        return null;
    }

    /**
     * Retrieves the percepts (sensory information) for a given agent.
     * This includes personal beliefs, surrounding tiles, and objects within range.
     *
     * @param agentName The name of the agent whose percepts are retrieved.
     * @return A collection of perceptual literals representing the agent's knowledge.
     */
    @Override
    public Collection<Literal> getPercepts(String agentName) {
        Agent agent = initializeAgentIfNeeded(agentName);

        return Stream.concat(
                personalBeliefsPercepts(agent).stream(),
                Stream.concat(
                        surroundingPercepts(agent).stream(),
                        inRangePercepts(agent).stream()
                )
        ).collect(Collectors.toList());
    }

    /**
     * Computes the personal belief percepts of an agent.
     * This includes position, orientation, objectives, and state information.
     *
     * @param agent The agent whose beliefs are being computed.
     * @return A collection of literals representing the agent’s personal beliefs.
     */
    @Override
    public Collection<Literal> personalBeliefsPercepts(Agent agent) {
        Collection<Literal> personalBeliefs = new ArrayList<>();

        personalBeliefs.add(Literal.parseLiteral(String.format("position(%d, %d)", agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY())));
        personalBeliefs.add(Literal.parseLiteral(String.format("orientation(%s)", agent.getPose().getOrientation().name().toLowerCase())));

        Pair<String, Vector2D> closest_objective = this.model.getClosestObjective(agent);
        if (agent.getPose().getPosition().equals(closest_objective.getSecond())) {
            agent.setState(closest_objective.getFirst());
        }

        personalBeliefs.add(Literal.parseLiteral(String.format("objective(%s)", closest_objective.getFirst())));
        personalBeliefs.add(Literal.parseLiteral(String.format("objective_position(%d,%d)", closest_objective.getSecond().getX(), closest_objective.getSecond().getY())));

        if (closest_objective.getFirst().equals("my_team_lost")) {
            personalBeliefs.add(Literal.parseLiteral("state(lost)"));
        } else if (closest_objective.getFirst().equals("my_team_won")) {
            personalBeliefs.add(Literal.parseLiteral("state(win)"));
        } else {
            personalBeliefs.add(Literal.parseLiteral(String.format("state(%s)", agent.getState())));
        }

        return personalBeliefs;
    }

    /**
     * Retrieves the percepts related to the agent's immediate surroundings.
     * Includes information about obstacles, allies, enemies, structures, and zone types.
     * This information is employed to conduct the movements logic and to apply
     * the zone and structure effect if there is one.
     *
     * @param agent The agent whose surrounding percepts are retrieved.
     * @return A collection of literals representing the agent's surroundings.
     */
    @Override
    public Collection<Literal> surroundingPercepts(Agent agent) {
        Collection<Literal> surroundings = model.getAgentSurroundingPositions(agent).entrySet().stream()
                .map(entry -> proximityPerceptFor(agent, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        surroundings.add(Literal.parseLiteral(String.format("zone_type(%s)", this.model.getCellByPosition(agent.getPose().getPosition()).getZoneType().name().toLowerCase())));

        MapStructure structure = this.model.getCellByPosition(agent.getPose().getPosition()).getStructure();
        if (structure != null & (structure instanceof Bridge)) {
            surroundings.add(Literal.parseLiteral(String.format("structure(%s, %d)", structure.getName().toLowerCase(), ((Bridge) structure).getSlipProbability())));
        }

        return surroundings;
    }

    /**
     * Generates a percept for an object or entity located in a given direction relative to the agent.
     *
     * @param agent     The agent perceiving the environment.
     * @param direction The direction relative to the agent.
     * @param position  The position being examined.
     * @return A literal representing the percept in the given direction.
     */
    private Literal proximityPerceptFor(Agent agent, Direction direction, Vector2D position) {
        Cell surroundingCell = this.model.getCellByPosition(position);

        if (surroundingCell == null || surroundingCell.getZoneType() == Zone.OUT_OF_MAP) {
            return Literal.parseLiteral(String.format("obstacle(%s)", direction.name().toLowerCase()));
        }

        Agent neighbour_agent = surroundingCell.getAgent();
        MapStructure structure = surroundingCell.getStructure();
        Resource resource = surroundingCell.getResource();

        if (neighbour_agent != null) {
            if (agent.getTeam() == neighbour_agent.getTeam()) {
                return Literal.parseLiteral(String.format("surrounding_ally(%s)", direction.name().toLowerCase()));
            } else {
                return Literal.parseLiteral(String.format("surrounding_enemy(%s)", direction.name().toLowerCase()));
            }
        } else if (structure != null) {
            if ((structure instanceof Tree tree) && tree.isDestroyed()) {
                return Literal.parseLiteral(String.format("free(%s)", direction.name().toLowerCase()));
            }
            return Literal.parseLiteral(String.format("%s(%s)", structure.getClass().getSimpleName().toLowerCase(), direction.name().toLowerCase()));
        } else if (resource != null) {
            return Literal.parseLiteral(String.format("%s(%s)", resource.getClass().getSimpleName().toLowerCase(), direction.name().toLowerCase()));
        } else {
            return Literal.parseLiteral(String.format("free(%s)", direction.name().toLowerCase()));
        }
    }

    /**
     * Retrieves percepts for objects or entities within the agent’s range.
     * Includes nearby agents, gates, trees, and princesses.
     * This information is used by the agents to acknowledge when they can
     * interact with artifacts or other agents.
     *
     * @param agent The agent whose in-range percepts are retrieved.
     * @return A collection of literals representing objects in range.
     */
    @Override
    public Collection<Literal> inRangePercepts(Agent agent) {
        Collection<Literal> in_range = new ArrayList<>();

        if (!(agent.getCarriedItem() != null & (agent.getCarriedItem() instanceof Princess))) {

            // Add percepts for agents in range
            in_range.addAll(model.getAgentNeighbours(agent, agent.getAttackRange()).stream()
                    .map(it -> {
                        String relation = (it.getTeam() == agent.getTeam()) ? "ally_in_range" : "enemy_in_range";
                        return String.format("%s(%s, %d)", relation, it.getName(), it.getHp());
                    })
                    .map(Literal::parseLiteral)
                    .collect(Collectors.toList()));

            // Add percepts for enemy gates in range
            in_range.addAll(model.getGateNeighbours(agent, "enemy", agent.getAttackRange()).stream()
                    .map(gate -> String.format("enemy_gate_in_range(%s, %d)", gate.getName(), gate.getHp()))
                    .map(Literal::parseLiteral)
                    .collect(Collectors.toList()));

            // Only gatherer agents perceive ally gate (to be repaired) and trees
            if (agent instanceof Gatherer) {
                // Add percepts for ally gates in range
                in_range.addAll(model.getGateNeighbours(agent, "ally", agent.getAttackRange()).stream()
                        .map(gate -> String.format("ally_gate_in_range(%s, %d)", gate.getName(), gate.getHp()))
                        .map(Literal::parseLiteral)
                        .collect(Collectors.toList()));

                // Add percepts for trees in range
                in_range.addAll(model.getTreeNeighbours(agent, agent.getAttackRange()).stream()
                        .map(tree -> String.format("tree_in_range(%s, %d)", tree.getName(), tree.getHp()))
                        .map(Literal::parseLiteral)
                        .collect(Collectors.toList()));
            }

            // Add percepts for princess in range
            in_range.addAll(model.getPrincessNeighbours(agent, "ally", 1).stream()
                    .map(princess -> String.format("ally_princess_in_range(%s)", princess.getName()))
                    .map(Literal::parseLiteral)
                    .collect(Collectors.toList()));

            // An agent perceives the enemy princes when she is outside of the base where she is prisoned
            if (!((this.model.getCellByPosition(agent.getPose().getPosition()).getZoneType() == Zone.BBASE) ||
                    (this.model.getCellByPosition(agent.getPose().getPosition()).getZoneType() == Zone.RBASE))) {

                in_range.addAll(model.getPrincessNeighbours(agent, "enemy", 1).stream()
                        .map(princess -> String.format("enemy_princess_in_range(%s)", princess.getName()))
                        .map(Literal::parseLiteral)
                        .collect(Collectors.toList()));
            }
        }


        return in_range;
    }

    /**
     * Executes an action requested by an agent.
     * Handles movement, attacks, healing, picking up resources (princesses),
     * and interactions with structures.
     *
     * @param ag     The name of the agent performing the action.
     * @param action The action structure containing the action type and parameters.
     * @return True if the action is successfully executed, false otherwise.
     */
    @Override
    public boolean executeAction(final String ag, final Structure action) {
        Agent agent = initializeAgentIfNeeded(ag);

        final boolean result;

        if (agent.getHp() <= 0 || action.toString().contains("respawn")) {
            agent.setHp(0);

            result = model.spawnAgent(agent);
            notifyModelChangedToView();

            try {
                Thread.sleep(threadSleepRespawn); // Proportional to normal sleep time
            } catch (InterruptedException ignored) {
            }

            this.model.resetAgent(agent);
            notifyModelChangedToView();

            return result;
        }


        if (absoluteMovementActions.containsValue(action)) {
            if (action.equals(absoluteMovementActions.get("random"))) {
                Direction randomAbsoluteDirection = getRandomAbsoluteDirection(agent);
                result = model.moveAgent(agent, 1, randomAbsoluteDirection);
                notifyModelChangedToView();
            } else {
                Direction absoluteDirection = getDirectionForAbsoluteMove(agent, action);
                result = model.moveAgent(agent, 1, absoluteDirection);
                notifyModelChangedToView();
            }
        } else if (action.toString().contains("attack_enemy")) {
            Optional<Agent> target = this.model.getAgentByName(action.getTerm(0).toString());

            boolean crit = Boolean.parseBoolean(action.getTerm(1).toString());

            result = model.attackAgent(agent, target.get(), crit);

            notifyModelChangedToView();
        } else if (action.toString().contains("heal_ally")) {
            Optional<Agent> target = this.model.getAgentByName(action.getTerm(0).toString());

            result = model.healAgent(agent, target.get());

            notifyModelChangedToView();
        } else if (action.toString().contains("attack_gate")) {
            Optional<Gate> target = this.model.getGateByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.attackGate(agent, target.get());
            notifyModelChangedToView();
        } else if (action.toString().contains("repair_gate")) {
            Optional<Gate> target = this.model.getGateByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.repairGate(agent, target.get());
            notifyModelChangedToView();
        } else if (action.toString().contains("attack_tree")) {
            Optional<Tree> target = this.model.getTreeByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.attackTree(agent, target.get());
            notifyModelChangedToView();
        } else if (action.toString().contains("pick_up_princess")) {
            Optional<Princess> target = this.model.getPrincessByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.pickUpPrincess(agent, target.get());
            notifyModelChangedToView();
        } else {
            logger.warning("Unknown action: " + action);
            return false;
        }

        try {
            Thread.sleep(threadSleep);
        } catch (InterruptedException ignored) {
        }

        return result;
    }
}