package mas_princess.environment;

import java.util.*;

public class GameWorld {

    private final int GRID_WIDTH = 50; // Map width
    private final int GRID_HEIGHT = 20; // Map height
    private Cell[][] map = new Cell[GRID_WIDTH][GRID_HEIGHT]; // 2D grid for the map

    private Map<String, AgentState> agents = new HashMap<>(); // Agent management
    private Map<Location, Resource> resources = new HashMap<>(); // Resource management
    private Map<Location, Structure> structures = new HashMap<>(); // Structure management

    private Princess princess1 = new Princess(100); // Princess for Team 1
    private Princess princess2 = new Princess(100); // Princess for Team 2

    // Initialize the game world
    public void initialize() {
        initializeMap();
        initializeAgents();
        initializeResources();
        initializeStructures();
    }

    // Initialize the map with zones
    private void initializeMap() {
        // Define base zones
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                map[x][y] = new Cell(Zone.BASE1);
                map[GRID_WIDTH - x - 1][GRID_HEIGHT - y - 1] = new Cell(Zone.BASE2);
            }
        }

        // Define river
        for (int x = 23; x < 26; x++) {
            for (int y = 0; y < 9; y++) {
                map[x][y] = new Cell(Zone.RIVER);
            }
            for (int y = 11; y < GRID_HEIGHT; y++) {
                map[x][y] = new Cell(Zone.RIVER);
            }
        }

        // Define walls and gates
        for (int y = 0; y < GRID_HEIGHT; y++) {
            if (y == GRID_HEIGHT / 2) {
                map[5][y] = new Cell(Zone.BATTLEFIELD); // Gate in the wall
                map[44][y] = new Cell(Zone.BATTLEFIELD); // Gate in the wall
                structures.put(new Location(5, y), new Structure("Gate", 100));
                structures.put(new Location(44, y), new Structure("Gate", 100));
            } else {
                map[5][y] = new Cell(Zone.BATTLEFIELD);
                map[44][y] = new Cell(Zone.BATTLEFIELD);
                structures.put(new Location(5, y), new Structure("Wall", 200));
                structures.put(new Location(44, y), new Structure("Wall", 200));
            }
        }

        // Fill remaining cells as battlefield
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD);
                }
            }
        }
    }

    // Initialize agents
    /*
    private void initializeAgents() {
        agents.put("gatherer1", new AgentState(new Location(1, 1), "Gatherer"));
        map[1][1].addContent("gatherer1");

        agents.put("warrior1", new AgentState(new Location(2, 2), "Warrior"));
        map[2][2].addContent("warrior1");
    }

    // Initialize resources
    private void initializeResources() {
        resources.put(new Location(3, 4), new Resource("Wood"));
        resources.put(new Location(5, 2), new Resource("Metal"));
        map[3][4].addContent(resources.get(new Location(3, 4)));
        map[5][2].addContent(resources.get(new Location(5, 2)));
    }

    

    // Access a specific cell in the map
    private Cell getCell(int x, int y) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            return map[x][y];
        }
        return null; // Out of bounds
    }

    // Check if an agent can enter a cell
    private boolean canEnterCell(AgentState agent, int x, int y) {
        Cell cell = getCell(x, y);
        if (cell == null || cell.getZoneType() == Zone.RIVER) {
            return false; // Impassable for most agents
        }
        return true; // Passable for all other zones
    }

    // Move an agent to a new location
    private boolean moveAgent(AgentState agent, int newX, int newY) {
        if (canEnterCell(agent, newX, newY)) {
            int oldX = agent.getLocation().getX();
            int oldY = agent.getLocation().getY();

            getCell(oldX, oldY).removeContent(agent);
            getCell(newX, newY).addContent(agent);

            agent.setLocation(new Location(newX, newY));
            return true;
        }
        return false; // Move failed
    }

    // Update agent perceptions
    public void updateAllAgentPerceptions(PrincessEnv env) {
        for (Map.Entry<String, AgentState> entry : agents.entrySet()) {
            String agentName = entry.getKey();
            AgentState agent = entry.getValue();

            env.clearPercepts(agentName);
            env.addPercept(agentName, jason.asSyntax.Literal.parseLiteral(
                "pos(" + agent.getLocation().getX() + "," + agent.getLocation().getY() + ")"
            ));

            updateNearbyPerceptions(env, agentName, agent.getLocation());
        }
    }

    // Update nearby perceptions for an agent
    private void updateNearbyPerceptions(PrincessEnv env, String agentName, Location location) {
        int x = location.getX();
        int y = location.getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                Cell cell = getCell(x + dx, y + dy);
                if (cell != null) {
                    for (Object obj : cell.getContents()) {
                        env.addPercept(agentName, jason.asSyntax.Literal.parseLiteral(
                            "cell(" + (x + dx) + "," + (y + dy) + "," + obj.toString() + ")"
                        ));
                    }
                }
            }
        }
    }
    */
    // Check if the game is over
    public boolean isGameOver() {
        return princess1.isSafe() || princess2.isSafe();
    }

    // Get the winning team
    public String getWinningTeam() {
        return princess1.isSafe() ? "Team 1" : "Team 2";
    }

    // Print the map for testing
    public void printMap() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                System.out.print(map[x][y].getZoneType().name().charAt(0) + " ");
            }
            System.out.println();
        }
    }
}
