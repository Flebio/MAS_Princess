package env.utils;

import env.agents.*;
import env.objects.resources.*;
import env.objects.structures.*;

public class Cell {
    private final int x;
    private final int y;
    private Zone zoneType;
    private MapStructure structure;
    private Resource resource;
    private Agent agent; // To track the agent present in this cell

    // Constructor
    public Cell(Zone zoneType, int x, int y) {
        this.zoneType = zoneType;
        this.structure = null; // No structure by default
        this.resource = null;  // No resource by default
        this.agent = null; // No agent by default
        this.x = x;
        this.y = y;
    }

    // Getters and Setters

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Zone getZoneType() {
        return zoneType;
    }
    public void setZoneType(Zone zoneType) {
        this.zoneType = zoneType;
    }

    public MapStructure getStructure() {
        return structure;
    }
    public void setStructure(MapStructure structure) {
        if (structure == null) {
            this.structure = null;
        }else if (this.isOccupied()) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.structure = structure;
        }
    }

    public Resource getResource() {
        return resource;
    }
    public void setResource(Resource resource) {
        if (resource == null) {
            this.resource = null;
        }else if (this.isOccupied()) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.resource = resource;
        }
    }

    // Agent
    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        if (agent == null) {
            this.agent = null;
        }else if (this.isOccupied()) {
            throw new IllegalStateException("A cell cannot contain both a structure, a resource or an agent.");
        } else {
            this.agent = agent;
        }
    }


    public boolean isOccupied() {
        return agent != null || structure != null || resource != null;
    }


    // Utility Methods

    public void clearAgent() {
        this.agent = null;
    }

    public void clearStructure() {
        this.structure = null;
    }

    public void clearResource() {
        this.resource = null;
    }

    private char getZoneSymbol() {
        return switch (zoneType) {
            case BBASE -> '1';
            case RBASE -> '2';
            case BATTLEFIELD -> 'F';
            case OUT_OF_MAP -> 'X';
            default -> ' ';
        };
    }

    private char getStructureSymbol() {
        if (structure instanceof Gate) return 'G';
        if (structure instanceof Wall) return 'M';
        if (structure instanceof Bridge) return 'B';
        return ' ';
    }

    private char getResourceSymbol() {
        if (resource instanceof Cake) return 'C';
        if (resource instanceof Wood) return 'L';
        if (resource instanceof Princess) return 'P';
        return ' ';
    }

    private char getAgentSymbol() {
        if (agent instanceof Warrior) return 'W';
        return ' ';
    }




    @Override
    public String toString() {
        if (structure != null) {
            return String.valueOf(getStructureSymbol());
        } else if (resource != null) {
            return String.valueOf(getResourceSymbol());
        } else if (agent != null) {
            return String.valueOf(getAgentSymbol());
        }
        return String.valueOf(getZoneSymbol());
    }
}
