package env.utils.utils_gui;

import env.agents.*;
import env.utils.*;
import java.util.Objects;

public class AgentKey {
    private final Class<? extends Agent> agentClass;
    private final boolean team;
    private final Orientation orientation;

    public AgentKey(Class<? extends Agent> agentClass, boolean team, Orientation orientation) {
        this.agentClass = Objects.requireNonNull(agentClass);
        this.team = team;
        this.orientation = Objects.requireNonNull(orientation);
    }

    public boolean isTeam() {
        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentKey agentKey = (AgentKey) o;
        return team == agentKey.team &&
                agentClass.equals(agentKey.agentClass) &&
                orientation == agentKey.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentClass, team, orientation);
    }
}
