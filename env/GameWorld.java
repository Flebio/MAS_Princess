package mas_princess.environment;

import java.util.*;

public class GameWorld {
    private Map<String, AgentState> agents = new HashMap<>();
    private Map<Location, Resource> resources = new HashMap<>();
 
 
//..............

    public boolean isGameOver() {
        return princess1.isSafe() || princess2.isSafe();
    }

    public String getWinningTeam() {
        return princess1.isSafe() ? "Team 1" : "Team 2";
    }
}
