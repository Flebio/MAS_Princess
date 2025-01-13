package mas_princess.env;

// Cell class
public class Cell {
    private Zone zoneType;
    private Structure structure; 

    public Cell(Zone zoneType) {
        this.zoneType = zoneType;
        this.structure = null; // No structure by default
    }

    public Zone getZoneType() {
        return zoneType;
    }

    public void setZoneType(Zone zoneType) {
        this.zoneType = zoneType;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }
}