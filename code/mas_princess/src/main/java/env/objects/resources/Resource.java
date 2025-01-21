package env.objects.resources;

// Resource class
public abstract class Resource {
    private final int quantity;
    private final int x;
    private final int y;

    public Resource(int quantity, int x, int y) {
        this.quantity = quantity;
        this.x = x;
        this.y = y;
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

    @Override
    public String toString() {
        return String.format("Resource[Name: %s, Quantity: %d, Position: [%d,%d]]",
                this.getClass().getSimpleName(), quantity, x, y);
    }
}

