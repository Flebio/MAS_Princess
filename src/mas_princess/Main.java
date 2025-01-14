package mas_princess;

import mas_princess.env.GameWorld;
import mas_princess.env.maps.BlackForest;

public class Main {
    public static void main(String[] args) {
        // Create a BlackForest map with custom dimensions
        GameWorld blackForest = new BlackForest(60, 20); // Custom dimensions
        blackForest.initialize();
        blackForest.printMap();
    }
}
