package env;

import env.utils.Vector2D;

/**
 * The MapView interface defines the necessary methods for displaying the map in the user interface.
 * Implementing classes must provide the logic for rendering the game map and its elements,
 * as well as updating the display based on changes in the map model.
 * <p>
 * The view is responsible for the visualization of agents, resources, and the overall game environment.
 */
public interface MapView {
    void notifyModelChanged();
    void triggerAttackView(Vector2D position);
    void triggerDamageView(Vector2D position);
    void triggerHealView(Vector2D position);
}
