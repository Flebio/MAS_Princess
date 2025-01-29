package env;

import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;

import env.utils.*;

import static env.utils.Direction.*;

import jason.RevisionFailedException;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BlackForestEnvironment extends Environment implements MapEnvironment {

    private static final Random RAND = new Random();
    static Logger logger = Logger.getLogger(BlackForestEnvironment.class.getName());
    private MapModel model;
    private BlackForestView view;

    @Override
    public void init(final String[] args) {
        this.model = new BlackForestModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        BlackForestView view = new BlackForestView(model);
        this.view = view;
        view.setVisible(true);
    }

    @Override
    public void notifyModelChangedToView() {
        view.notifyModelChanged();
    }

    @Override
    public Agent initializeAgentIfNeeded(String agentName) {
        Optional<Agent> optionalAgent = this.model.getAgentByName(agentName);

//        if (!optionalAgent.isEmpty() && optionalAgent.get().getHp() == 0) {
//            System.out.println("Eccolo:  " + optionalAgent);
//        }
        if (optionalAgent.isEmpty()) {
            //System.out.println("NECESSARIO");
            String[] parts = agentName.split("_");

            String type = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);  // Capitalize first letter of type
            String team = parts[1];  // Extract team (after underscore)

            try {
                // Assuming the class names are "Warrior", "Archer", etc.
                Class<?> agentClass = Class.forName("env.agents." + type);  // Fully qualified class name

                // Create an instance of the agent class
                Constructor<?> constructor = agentClass.getConstructor(String.class, boolean.class);

                // Instantiate the object
                Agent agent = (Agent) constructor.newInstance(agentName, team.contains("r"));  // 'r' -> true, 'b' -> false

                // Add agent to the model
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

    @Override
    public Collection<Literal> getPercepts(String agentName) {
        Agent agent = initializeAgentIfNeeded(agentName);
        //        logger.info("Fetching percepts for agent: " + agentName);

        // Combine percepts: personal beliefs, surrounding tiles and neighbors.
        return Stream.concat(
                personalBeliefsPercepts(agent).stream(),
                        Stream.concat(
                                surroundingPercepts(agent).stream(),
                                Stream.concat(
                                        inRangePercepts(agent).stream(),
                                        inSightPercepts(agent).stream()
                                        )
                        )
        ).collect(Collectors.toList());
    }


    @Override
    public Collection<Literal> personalBeliefsPercepts(Agent agent) {
        Collection<Literal> personalBeliefs = new ArrayList<>();

        //personalBeliefs.add(Literal.parseLiteral(String.format("hp(%d)", agent.getHp())));
        personalBeliefs.add(Literal.parseLiteral(String.format("position(%d, %d)", agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY())));
        personalBeliefs.add(Literal.parseLiteral(String.format("orientation(%s)", agent.getPose().getOrientation().name().toLowerCase())));

        Pair<String, Vector2D> closest_objective = this.model.getClosestObjective(agent);
        if (agent.getPose().getPosition().equals(closest_objective.getSecond())) {
            agent.setState(closest_objective.getFirst());
//            closest_objective = this.model.getClosestObjective(agent);
        }

        personalBeliefs.add(Literal.parseLiteral(String.format("state(%s)", agent.getState())));
        personalBeliefs.add(Literal.parseLiteral(String.format("objective(%s)", closest_objective.getFirst())));
        personalBeliefs.add(Literal.parseLiteral(String.format("objective_position(%d,%d)", closest_objective.getSecond().getX(), closest_objective.getSecond().getY())));

        // If for each agent we get its position and team we can give information
        // about where each ally and enemy are
        // for ag in this.model.getAllAgents():

        return personalBeliefs;
    }


    @Override
    public Collection<Literal> surroundingPercepts(Agent agent) {
        Collection<Literal> surroundings = model.getAgentSurroundingPositions(agent).entrySet().stream()
                .map(entry -> proximityPerceptFor(agent, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        surroundings.add(Literal.parseLiteral(String.format("zone_type(%s)", this.model.getCellByPosition(agent.getPose().getPosition()).getZoneType().name().toLowerCase())));
//        logger.info("Surroundings(" + agent.getName() + ") -> " + surroundings);
        return surroundings;
    }

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
            return Literal.parseLiteral(String.format("%s(%s)", structure.getClass().getSimpleName().toLowerCase(), direction.name().toLowerCase()));
        } else if (resource != null) {
            return Literal.parseLiteral(String.format("%s(%s)", resource.getClass().getSimpleName().toLowerCase(), direction.name().toLowerCase()));
        } else {
            return Literal.parseLiteral(String.format("free(%s)", direction.name().toLowerCase()));
        }
    }

    @Override
    public Collection<Literal> inRangePercepts(Agent agent) {
        Collection<Literal> in_range = new ArrayList<>();

        // Add percepts for agents in range
        in_range.addAll(model.getAgentNeighbours(agent, agent.getAttackRange()).stream()
                .map(it -> {
                    String relation = (it.getTeam() == agent.getTeam()) ? "ally_in_range" : "enemy_in_range";
                    return String.format("%s(%s, %d)", relation, it.getName(), it.getHp());
                })
                .map(Literal::parseLiteral)
                .collect(Collectors.toList()));

        // Add percepts for enemy gates in range
        in_range.addAll(model.getEnemyGateNeighbours(agent, agent.getAttackRange()).stream()
                .map(gate -> String.format("enemy_gate_in_range(%s, %d)", gate.getName(), gate.getHp()))
                .map(Literal::parseLiteral)
                .collect(Collectors.toList()));

        // Add percepts for trees in range
        in_range.addAll(model.getTreeNeighbours(agent, agent.getAttackRange()).stream()
                .map(tree -> String.format("tree_in_range(%s, %d)", tree.getName(), tree.getHp()))
                .map(Literal::parseLiteral)
                .collect(Collectors.toList()));
        //System.out.println(model.getEnemyGateNeighbours(agent, agent.getAttackRange()));
        return in_range;
    }


    @Override
    public Collection<Literal> inSightPercepts(Agent agent) {
        Collection<Literal> in_sight = model.getAgentNeighbours(agent, 5).stream()
                .map(it -> {
                    String relation = (it.getTeam() == agent.getTeam()) ? "ally_in_sight" : "enemy_in_sight";
                    return String.format("%s(%s, %d)", relation, it.getName(), it.getHp());
                })
                .map(Literal::parseLiteral)
                .collect(Collectors.toList());

//        logger.info("in_sight(" + agent.getName() + ") -> " + in_sight);
        return in_sight;
    }
    @Override
    public boolean executeAction(final String ag, final Structure action) {
        Agent agent = initializeAgentIfNeeded(ag);

        //logger.info(ag + " does action: " + action.toString());

        final boolean result;
        if (movementActions.containsValue(action)) {
            if (action.equals(movementActions.get("random"))) {
                Direction randomDirection = Direction.random();
                //logger.info("Chosen direction for random movement: " + randomDirection);
                result = model.moveAgent(agent, 1, randomDirection);
                notifyModelChangedToView();
            } else {
                Direction direction = getDirectionForAction(action, movementActions);
                result = model.moveAgent(agent, 1, direction);
                notifyModelChangedToView();
            }
        } else if (absoluteMovementActions.containsValue(action)) {
            if (action.equals(absoluteMovementActions.get("random"))) {
                Direction randomAbsoluteDirection = getRandomAbsoluteDirection(agent);
                //logger.info("Executing random absolute movement resolved to " + randomAbsoluteDirection);
                result = model.moveAgent(agent, 1, randomAbsoluteDirection);
                notifyModelChangedToView();
            } else {
                Direction absoluteDirection = getDirectionForAbsoluteMove(agent, action);
                //logger.info("Executing absolute movement: " + action + " resolved to " + absoluteDirection);
                result = model.moveAgent(agent, 1, absoluteDirection);
                notifyModelChangedToView();
            }
        } else if (action.toString().contains("attack_enemy")) {
            Agent target = initializeAgentIfNeeded(action.getTerm(0).toString());
            result = model.attackAgent(agent, target);
            notifyModelChangedToView();
        }else if (action.toString().contains("attack_gate")) {
            Optional<Gate> target = this.model.getGateByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.attackGate(agent, target.get());
            notifyModelChangedToView();
        }else if (action.toString().contains("attack_tree")) {
            Optional<Tree> target = this.model.getTreeByName(action.getTerm(0).toString());
            if (target.get() == null) {
                return false;
            }
            result = model.attackTree(agent, target.get());
            notifyModelChangedToView();
        } else if (action.toString().contains("respawn")) {
            result = model.respawnAgent(agent);
//            try { // Wait time to be able to get back in the game.
//                Thread.sleep(5000L);
//            } catch (InterruptedException ignored) {
//            }
        } else if (action.toString().contains("pick_up_princess")) {
            // CONTINUARE result = model.pickup_princess(agent);
            // AGGIUNGERE FUNZIONE IN GAMEMAP
        } else{
            logger.warning("Unknown action: " + action);
            return false;
        }

        // DA AGGIUNGERE BUILD LADDER E REPAIR GATE

        try {
//            Thread.sleep(1000L / model.getFPS());
            if (action.toString().contains("respawn")) {
                Thread.sleep(5000L / model.getFPS());
            } else {
//                Thread.sleep(500L / model.getFPS());
                Thread.sleep(100L / model.getFPS());
            }
        } catch (InterruptedException ignored) {
        }
        return result;
    }

    // Movement actions collection
    private static final Map<String, Literal> movementActions = Map.of(
            FORWARD.name().toLowerCase(), Literal.parseLiteral("move(" + FORWARD.name().toLowerCase() + ")"),
            RIGHT.name().toLowerCase(), Literal.parseLiteral("move(" + RIGHT.name().toLowerCase() + ")"),
            LEFT.name().toLowerCase(), Literal.parseLiteral("move(" + LEFT.name().toLowerCase() + ")"),
            BACKWARD.name().toLowerCase(), Literal.parseLiteral("move(" + BACKWARD.name().toLowerCase() + ")"),
            "random", Literal.parseLiteral("move(random)")
    );

    private Direction getDirectionForAction(Structure action, Map<String, Literal> mapping) {
        return mapping.entrySet().stream()
                .filter(entry -> entry.getValue().equals(action))
                .map(entry -> Direction.valueOf(entry.getKey().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action does not map to any direction: " + action));
    }

    // Map for absolute movement actions
    private static final Map<String, Literal> absoluteMovementActions = Map.of(
            "up", Literal.parseLiteral("absolute_move(up)"),
            "down", Literal.parseLiteral("absolute_move(down)"),
            "left", Literal.parseLiteral("absolute_move(left)"),
            "right", Literal.parseLiteral("absolute_move(right)"),
            "random", Literal.parseLiteral("absolute_move(random)")
    );

    // Mapping of absolute movement based on current orientation
    private static final Map<Orientation, Map<String, Direction>> absoluteMovementMapping = Map.of(
            Orientation.NORTH, Map.of(
                    "left", Direction.LEFT,
                    "right", Direction.RIGHT,
                    "up", Direction.FORWARD,
                    "down", Direction.BACKWARD
            ),
            Orientation.SOUTH, Map.of(
                    "left", Direction.RIGHT,
                    "right", Direction.LEFT,
                    "up", Direction.BACKWARD,
                    "down", Direction.FORWARD
            ),
            Orientation.EAST, Map.of(
                    "left", Direction.BACKWARD,
                    "right", Direction.FORWARD,
                    "up", Direction.LEFT,
                    "down", Direction.RIGHT
            ),
            Orientation.WEST, Map.of(
                    "left", Direction.FORWARD,
                    "right", Direction.BACKWARD,
                    "up", Direction.RIGHT,
                    "down", Direction.LEFT
            )
    );

    private Direction getDirectionForAbsoluteMove(Agent agent, Structure action) {
        Orientation currentOrientation = agent.getPose().getOrientation();

        return absoluteMovementMapping.getOrDefault(currentOrientation, Map.of()).entrySet().stream()
                .filter(entry -> absoluteMovementActions.get(entry.getKey()).equals(action))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action does not map to any absolute direction: " + action));
    }

    private Direction getRandomAbsoluteDirection(Agent agent) {
        Orientation currentOrientation = agent.getPose().getOrientation();
        List<Direction> possibleDirections = new ArrayList<>(absoluteMovementMapping.get(currentOrientation).values());
        Collections.shuffle(possibleDirections);
        return possibleDirections.get(0);
    }

}
