package env;

public interface MapView {
    MapModel getModel();
    void notifyModelChanged();
}
