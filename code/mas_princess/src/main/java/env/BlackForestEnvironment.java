package env;

import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;

import env.utils.*;

import static env.utils.Direction.*;

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
//    private MapView view;

    @Override
    public void init(final String[] args) {
        this.model = new BlackForestModel(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
//        BlackForestView view = new BlackForestView(model);
//        this.view = view;
//        view.setVisible(true);
    }

//    @Override
//    public void notifyModelChangedToView() {
//        view.notifyModelChanged();
//    }

    @Override
    public Agent initializeAgentIfNeeded(String agentName) {
        Optional<Agent> optionalAgent = this.model.getAgentByName(agentName);

        if (optionalAgent.isEmpty()) {
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

        // Combine percepts: surrounding tiles, neighbors, and personal beliefs
        return Stream.concat(
                personalBeliefsPercepts(agent).stream(),
                Stream.concat(
                        surroundingPercepts(agent).stream(),
                        neighboursPercepts(agent).stream()
                )
        ).collect(Collectors.toList());
    }

    @Override
    public Collection<Literal> personalBeliefsPercepts(Agent agent) {
        Collection<Literal> personalBeliefs = new ArrayList<>();

        personalBeliefs.add(Literal.parseLiteral(String.format("hp(%d)", agent.getHp())));
        personalBeliefs.add(Literal.parseLiteral(String.format("zone_type(%s)", this.model.getCellByPosition(agent.getPose().getPosition()).getZoneType().name().toLowerCase())));
        personalBeliefs.add(Literal.parseLiteral(String.format("position(%d, %d)", agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY())));
        personalBeliefs.add(Literal.parseLiteral(String.format("orientation(%s)", agent.getPose().getOrientation().name().toLowerCase())));

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
//        logger.info("Surroundings(" + agent.getName() + ") -> " + surroundings);
        return surroundings;
    }

    private Literal proximityPerceptFor(Agent agent, Direction direction, Vector2D position) {
        Cell surroundingCell = this.model.getCellByPosition(position);

        if (surroundingCell == null) {
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
    public Collection<Literal> neighboursPercepts(Agent agent) {
        Collection<Literal> in_range = model.getAgentNeighbours(agent).stream()
                .map(it -> {
                    String relation = (it.getTeam() == agent.getTeam()) ? "ally_in_range" : "enemy_in_range";
                    return String.format("%s(%s)", relation, it.getName());
                })
                .map(Literal::parseLiteral)
                .collect(Collectors.toList());

//        logger.info("in_range(" + agent.getName() + ") -> " + in_range);
        return in_range;
    }

    @Override
    public boolean executeAction(final String ag, final Structure action) {
        Agent agent = initializeAgentIfNeeded(ag);

        System.out.println("Warrior name:" + ag);
        System.out.println(ag == "warrior_b1");
        if (ag.equals("archer_b1")) {
            this.model.printAgentList(logger);
            this.model.printMap(logger);
        }

        logger.info(ag + " does action: " + action.toString());

        final boolean result;
        if (movementActions.containsValue(action)) {
            if (action.equals(movementActions.get("random"))) {
                Direction randomDirection = Direction.random();
                logger.info("Chosen direction for random movement: " + randomDirection);
                result = model.moveAgent(agent, 1, randomDirection);
            } else {
                Direction direction = getDirectionForAction(action, movementActions);
                result = model.moveAgent(agent, 1, direction);
            }
        } else {
            logger.warning("Unknown action: " + action);
            return false;
        }

        // Update the view and simulate delay for FPS

//        try {
//            Thread.sleep(1000L / model.getFPS());
//        } catch (InterruptedException ignored) {
//            logger.info("Arturo penelli");
//        }

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
}
