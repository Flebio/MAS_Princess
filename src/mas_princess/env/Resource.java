package mas_princess.env;

// Resource class
public abstract class Resource {
    private final String name;
    private final int quantity;
    private final int x;
    private final int y;

    public Resource(String name, int quantity, int x, int y) {
        this.name = name;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

