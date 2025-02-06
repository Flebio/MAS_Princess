package env.utils;

import env.agents.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbsoluteMovement {

        // Movement actions collection
        public static final Map<String, Literal> absoluteMovementActions = Map.of(
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

        /**
         * Determines the absolute movement direction based on the agent’s current orientation and action.
         * With the term absolute, we mean it to be relative to whom runs the application.
         *
         * @param agent The agent performing the movement.
         * @param action The movement action requested.
         * @return The corresponding movement direction.
         */
        public static Direction getDirectionForAbsoluteMove(Agent agent, Structure action) {
            Orientation currentOrientation = agent.getPose().getOrientation();

            return absoluteMovementMapping.getOrDefault(currentOrientation, Map.of()).entrySet().stream()
                    .filter(entry -> absoluteMovementActions.get(entry.getKey()).equals(action))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Action does not map to any absolute direction: " + action));
        }

        /**
         * Selects a random absolute movement direction for the agent based on its current orientation.
         *
         * @param agent The agent making the movement.
         * @return A randomly selected direction.
         */
        public static Direction getRandomAbsoluteDirection(Agent agent) {
            Orientation currentOrientation = agent.getPose().getOrientation();
            List<Direction> possibleDirections = new ArrayList<>(absoluteMovementMapping.get(currentOrientation).values());
            Collections.shuffle(possibleDirections);
            return possibleDirections.get(0);
        }
}