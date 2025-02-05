package env;

import env.utils.Vector2D;

public interface MapView {
    MapModel getModel();
    void notifyModelChanged();
    void triggerAttackView(Vector2D position);
    void triggerDamageView(Vector2D position);
    void triggerHealView(Vector2D position);
}
