package env.utils;

import env.agents.*;
import env.objects.resources.*;
import env.objects.structures.*;

/**
 * Represents a single cell on the game map.  Each cell can contain a zone type, a structure,
 * a resource, and an agent.  This class manages the state of the cell and provides methods for
 * interacting with its contents.
 */
public class Cell {
    private final int x;
    private final int y;
    private Zone zoneType;
    private MapStructure structure;
    private Resource resource;
    private Agent agent;

    /**
     * Constructs a new Cell with the specified zone type and coordinates.
     *
     * @param zoneType the type of zone this cell belongs to.
     * @param x        the x-coordinate of the cell.
     * @param y        the y-coordinate of the cell.
     */
    public Cell(Zone zoneType, int x, int y) {
        this.zoneType = zoneType;
        this.structure = null; // No structure by default
        this.resource = null;  // No resource by default
        this.agent = null; // No agent by default
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of this cell.
     *
     * @return the x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of this cell.
     *
     * @return the y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the position of this cell as a {@code Vector2D}.
     *
     * @return a {@code Vector2D} representing the cell's position.
     */
    public Vector2D getPosition() {
        return new Vector2D(x, y);
    }

    /**
     * Returns the zone type of this cell.
     *
     * @return the zone type.
     */
    public Zone getZoneType() {
        return zoneType;
    }

    /**
     * Sets the zone type of this cell.
     *
     * @param zoneType the new zone type.
     */
    public void setZoneType(Zone zoneType) {
        this.zoneType = zoneType;
    }

    /**
     * Returns the structure contained in this cell.
     *
     * @return the structure, or {@code null} if no structure is present.
     */
    public MapStructure getStructure() {
        return structure;
    }

    /**
     * Sets the structure contained in this cell.  Throws an exception if the cell already
     * contains a resource or an agent.
     *
     * @param structure the structure to place in the cell.
     * @throws IllegalStateException if the cell is already occupied by a resource or an agent.
     */
    public void setStructure(MapStructure structure) {
        if (structure == null) {
            this.structure = null;
        } else if (this.isOccupied(null, null)) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.structure = structure;
        }
    }

    /**
     * Returns the resource contained in this cell.
     *
     * @return the resource, or {@code null} if no resource is present.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Sets the resource contained in this cell. Throws an exception if the cell already
     * contains a structure or an agent.
     *
     * @param resource the resource to place in the cell.
     * @throws IllegalStateException if the cell is already occupied by a structure or an agent.
     */
    public void setResource(Resource resource) {
        if (resource == null) {
            this.resource = null;
        } else if (this.isOccupied(null, resource)) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.resource = resource;
        }
    }

    /**
     * Returns the agent contained in this cell.
     *
     * @return the agent, or {@code null} if no agent is present.
     */
    public Agent getAgent() {
        return this.agent;
    }

    /**
     * Sets the agent contained in this cell. Throws an exception if the cell already
     * contains a structure or a resource.
     *
     * @param agent the agent to place in the cell.
     * @throws IllegalStateException if the cell is already occupied by a structure or a resource.
     */
    public void setAgent(Agent agent) {
        if (agent == null) {
            this.agent = null;
        } else if (this.isOccupied(agent, null)) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.agent = agent;
        }
    }

    /**
     * Checks if this cell is occupied by a structure, resource, or agent.  Considers special cases
     * for walkable structures (destroyed trees/gates, gates of the same team) and agents carrying resources.
     *
     * @param movingAgent   The agent that may be moving into the cell (used for gate checks).
     * @param movingResource The resource that may be moving into the cell (used for carried resource checks).
     * @return {@code true} if the cell is occupied, {@code false} otherwise.
     */
    public boolean isOccupied(Agent movingAgent, Resource movingResource) {
        // Check if the cell is completely empty
        if (this.agent == null && this.structure == null && this.resource == null) {
            return false;
        }

        if (structure != null && (structure instanceof Empty) && movingResource != null && (movingResource instanceof Princess)) {
            return false;
        }

        // If there is a structure, validate its conditions
        if (structure != null && movingAgent != null) {
            if (structure.isWalkable()) {
                // Is a Tree and destroyed -> Not occupied
                if (structure instanceof Tree && ((Tree) structure).isDestroyed()) {
                    return false;
                }

                // Is a Gate and destroyed -> Not occupied
                if (structure instanceof Gate && ((Gate) structure).isDestroyed()) {
                    return false;
                } else if (structure instanceof Gate && agent != null && agent.getTeam() != movingAgent.getTeam()) {
                    return false;
                }

                // Is a Gate and belongs to the same team as the agent -> Not occupied
                if (structure instanceof Gate && ((Gate) structure).getTeam() == movingAgent.getTeam()) {
                    return false;
                }

                // Other cases -> Not occupied
                return false;
            }
        } else if (agent != null && movingResource != null) {
            // If there's an agent in the cell and the moving resource has the same team -> not occupato
            if (agent.getCarriedItem().equals(movingResource)) {
                return false;
            }
        }

        // If there is a resource, or the conditions above are not met -> Occupied
        return true;
    }

    /**
     * Clears the agent from this cell.
     */
    public void clearAgent() {
        this.agent = null;
    }

    /**
     * Clears the structure from this cell.
     */
    public void clearStructure() {
        this.structure = null;
    }

    /**
     * Clears the resource from this cell.
     */
    public void clearResource() {
        this.resource = null;
    }

    /**
     * Returns a character symbol representing the zone type of this cell.
     *
     * @return a character symbol for the zone type.
     */
    private char getZoneSymbol() {
        return switch (zoneType) {
            case BBASE -> '1';
            case RBASE -> '2';
            case BATTLEFIELD -> 'F';
            case OUT_OF_MAP -> 'X';
            default -> ' ';
        };
    }

    /**
     * Returns a character symbol representing the structure in this cell.
     *
     * @return a character symbol for the structure, or ' ' if no structure is present.
     */
    private char getStructureSymbol() {
        if (structure instanceof Gate) return 'G';
        if (structure instanceof Wall) return 'M';
        if (structure instanceof Bridge) return 'B';
        if (structure instanceof Empty) return 'E';
        return ' ';
    }

    /**
     * Returns a character symbol representing the resource in this cell.
     *
     * @return a character symbol for the resource, or ' ' if no resource is present.
     */
    private char getResourceSymbol() {
        if (resource instanceof Princess) return 'P';
        return ' ';
    }

    /**
     * Returns a character symbol representing the agent in this cell.
     *
     * @return a character symbol for the agent, or ' ' if no agent is present.
     */
    private char getAgentSymbol() {
        if (agent instanceof Warrior) return 'W';
        if (agent instanceof Archer) return 'A';
        if (agent instanceof Gatherer) return 'H';
        if (agent instanceof Priest) return 'D';
        return ' ';
    }

    /**
     * Returns a string representation of the cell content.
     *
     * @return a string representation of the cell content.
     */
    @Override
    public String toString() {
        if (resource != null) {
            return String.valueOf(getResourceSymbol());
        } else if (structure != null) {
            return String.valueOf(getStructureSymbol());
        } else if (agent != null) {
            return String.valueOf(getAgentSymbol());
        }
        return String.valueOf(getZoneSymbol());
    }
}
