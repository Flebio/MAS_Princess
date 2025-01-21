package env;

import env.*;
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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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

            logger.info("Type: " + type + ", Team: " + team);

            try {
                // Assuming the class names are "Warrior", "Archer", etc.
                Class<?> agentClass = Class.forName("env.agents." + type);  // Fully qualified class name

                // Create an instance of the agent class
                Constructor<?> constructor = agentClass.getConstructor(String.class, boolean.class);

                // Instantiate the object
                Agent agent = (Agent) constructor.newInstance(agentName, team.equals("r"));  // 'r' -> true, 'b' -> false

                logger.info("CAZZZO   " + agent.toString());
                // Add agent to the model
                this.model.spawnAgent(agent);

                logger.info("CAZZZO_2   " + agent.toString());

                this.model.printAgentList();
                this.model.printMap();

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

        // Combine percepts: surrounding tiles and neighbors
        return Stream.concat(
                surroundingPercepts(agent).stream(),
                neighboursPercepts(agent).stream()
        ).collect(Collectors.toList());
    }

    @Override
    public Collection<Literal> surroundingPercepts(Agent agent) {
        Collection<Literal> culo = model.getAgentSurroundingPositions(agent).entrySet().stream()
                .map(entry -> proximityPerceptFor(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        logger.info("CULO" + culo);
        return culo;
    }

    private Literal proximityPerceptFor(Direction direction, Vector2D position) {
        Cell surroundingCell = this.model.getCellByPosition(position);

        if (surroundingCell == null) {
            return Literal.parseLiteral(String.format("OUT OF MAP [Direction -> %s]", direction.name().toLowerCase()));
        }

        Agent agent = surroundingCell.getAgent();
        MapStructure structure = surroundingCell.getStructure();
        Resource resource = surroundingCell.getResource();
// FARE QUESTA FUNCTION
        if (agent != null) {
            return Literal.parseLiteral(String.format(agent.toString(), "[Direction -> %s]", direction.name().toLowerCase()));
        } else if (structure != null) {
            return Literal.parseLiteral(String.format(structure.toString(), "[Direction -> %s]", direction.name().toLowerCase()));
        } else if (resource != null) {
            return Literal.parseLiteral(String.format(resource.toString(), "[Direction -> %s]", direction.name().toLowerCase()));
        } else {
            return Literal.parseLiteral(String.format("empty CELL [Direction -> %s]", direction.name().toLowerCase()));
        }
    }

    @Override
    public Collection<Literal> neighboursPercepts(Agent agent) {
        Collection<Literal> culo = model.getAgentNeighbours(agent).stream()
                .map(it -> String.format("[Neighbour -> %s]", it))
                .map(Literal::parseLiteral)
                .collect(Collectors.toList());
        logger.info("CULO_2" + culo);
        return culo;
    }

    @Override
    public boolean executeAction(final String ag, final Structure action) {
        Agent agent = initializeAgentIfNeeded(ag);
        logger.info("AZIONE  STRUTTURA" + action.toString());

        final boolean result;
        if (movementActions.containsValue(action)) {
            if (action.equals(movementActions.get("random"))) {
                logger.info("AZIONE 1");
                Direction randomDirection = Direction.random();
                result = model.moveAgent(agent, 1, randomDirection);
            } else {
                logger.info("AZIONE 2");
                Direction direction = getDirectionForAction(action);
                result = model.moveAgent(agent, 1, direction);
            }
        } else {
            logger.info("AZIONE 3");
            logger.warning("Unknown action: " + action);
            return false;
        }

        // Update the view and simulate delay for FPS
        this.model.printAgentList();
        this.model.printMap();

        try {
            Thread.sleep(1000L / model.getFPS());
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
    private Direction getDirectionForAction(Structure action) {
        return movementActions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(action))
                .map(entry -> Direction.valueOf(entry.getKey().toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action does not map to any direction: " + action));
    }
}
