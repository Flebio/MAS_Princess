package env;

import env.utils.*;
import env.utils.utils_gui.*;
import env.objects.structures.*;
import env.objects.resources.*;
import env.agents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The `BlackForestView` class represents the graphical user interface (GUI) for the Black Forest simulation.
 * It displays the map, agent statuses, and resource information.
 * This class extends `JFrame` and implements the `MapView` interface.
 *
 * As part of the Model-View-Controller (MVC) pattern, this class serves as the **View**,
 * responsible for rendering the simulation state and presenting it to the user.
 * It receives updates from the **Model** and interacts with the **Controller** to reflect changes.
 */
public class BlackForestView extends JFrame implements MapView {

    private final MapModel model; // The map model
    private final Map<Vector2D, JLabel> cellsGrid = new HashMap<>(); // Mapping of positions to grid labels
    private final Map<Zone, ImageIcon> zoneSprites = new EnumMap<>(Zone.class); // Mapping of zone types to sprites
    private final Map<AgentKey, java.util.List<ImageIcon>> agentSprites = new HashMap<>();
    private final java.util.List<ImageIcon> battlefieldSprites = new ArrayList<>(); // List of possible battlefield sprites
    private final Map<Vector2D, ImageIcon> randomizedBattlefieldSprites = new HashMap<>(); // Randomized battlefield sprites
    private final DefaultListModel<Agent> agentListModel = new DefaultListModel<>(); // Model for the agent list
    private final JList<Agent> agentList = new JList<>(agentListModel); // List to display agents
    private JPanel agentStatusContainer; // Panel to display agent statuses
    private final Map<Agent, Integer> agentAnimationFrames = new HashMap<>();
    private final javax.swing.Timer animationTimer = new javax.swing.Timer(200, e -> updateAnimationFrames());

    private final Map<Class<? extends Resource>, ImageIcon> resourceSprites = new HashMap<>();
    private final Map<String, ImageIcon> princessSprites = new HashMap<>();

    private JLabel woodBlueLabel, woodRedLabel, princessBlueLabel, princessRedLabel;
    private JProgressBar gateB1HpBar, gateB2HpBar, gateR1HpBar, gateR2HpBar;
    private int outOfMapAnimationFrame = 0; // Track animation frame for OUT_OF_MAP zone
    private final ImageIcon[] outOfMapSprites = new ImageIcon[2]; // Hold river sprites for animation
    private final ImageIcon[] outOfMapSpritesE = new ImageIcon[2];
    private final ImageIcon[] outOfMapSpritesW = new ImageIcon[2];
    private final ImageIcon[] outOfMapSpritesS = new ImageIcon[2];
    private final ImageIcon[] outOfMapSpritesSW = new ImageIcon[2];
    private final ImageIcon[] outOfMapSpritesSE = new ImageIcon[2];

    private final javax.swing.Timer outOfMapAnimationTimer = new javax.swing.Timer(500, e -> {
        outOfMapAnimationFrame = (outOfMapAnimationFrame + 1) % 2; // Toggle between 0 and 1
        refreshBackground(); // Refresh map with the new frame
    });

    private final ImageIcon crownIcon = new ImageIcon(getClass().getResource("/sprites/crown.png"));

    /**
     * Constructor for the `BlackForestView`.
     *
     * @param model The `MapModel` instance to be displayed.
     * @throws NullPointerException If the provided `model` is null.
     */
    public BlackForestView(MapModel model) {
        this.model = Objects.requireNonNull(model);

        // Load sprites for zones and agents
        loadSprites();
        animationTimer.start();
        outOfMapAnimationTimer.start();

        // Set up the main container
        JPanel contentPane = new JPanel(new BorderLayout());

        // Create and populate the map grid
        JPanel gridPanel = createGridPanel();

        // Create the agent status panel
        JPanel agentStatusPanel = createAgentStatusPanel();

        // Create the resource panel
        JPanel resourcePanel = createResourcePanel();


        // Add panels to the main container
        contentPane.add(gridPanel, BorderLayout.CENTER); // Map grid in the center
        contentPane.add(agentStatusPanel, BorderLayout.SOUTH); // Agent statuses at the bottom
        contentPane.add(resourcePanel, BorderLayout.WEST); // Add it to the left side

        setContentPane(contentPane);
        pack();

        setResizable(false);

        // Refresh the view initially
        refreshBackground();
        updateAgentList();
    }

    private void loadSprites() {
        loadZoneSprites();
        loadAgentSprites();
        loadResourceSprites();
    }
    /**
     * Loads the sprites for the different zone types.
     */
    private void loadZoneSprites() {
        zoneSprites.put(Zone.BBASE, new ImageIcon(getClass().getResource("/sprites/bbase.png")));
        zoneSprites.put(Zone.RBASE, new ImageIcon(getClass().getResource("/sprites/rbase.png")));

        // Load animated sprites for OUT_OF_MAP
        outOfMapSprites[0] = new ImageIcon(getClass().getResource("/sprites/river1.png"));
        outOfMapSprites[1] = new ImageIcon(getClass().getResource("/sprites/river2.png"));
        outOfMapSpritesE[0] = new ImageIcon(getClass().getResource("/sprites/river_w.png"));
        outOfMapSpritesE[1] = new ImageIcon(getClass().getResource("/sprites/river_w2.png"));
        outOfMapSpritesW[0] = new ImageIcon(getClass().getResource("/sprites/river_e.png"));
        outOfMapSpritesW[1] = new ImageIcon(getClass().getResource("/sprites/river_e2.png"));
        outOfMapSpritesS[0] = new ImageIcon(getClass().getResource("/sprites/river_n.png"));
        outOfMapSpritesS[1] = new ImageIcon(getClass().getResource("/sprites/river_n2.png"));
        outOfMapSpritesSW[0] = new ImageIcon(getClass().getResource("/sprites/river_c_sw.png"));
        outOfMapSpritesSW[1] = new ImageIcon(getClass().getResource("/sprites/river_c_sw2.png"));
        outOfMapSpritesSE[0] = new ImageIcon(getClass().getResource("/sprites/river_c_se.png"));
        outOfMapSpritesSE[1] = new ImageIcon(getClass().getResource("/sprites/river_c_se2.png"));

        // Add multiple battlefield sprites
        for (int i = 0; i < 11; i++) {
            battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield.png")));
        }
        for (int i = 0; i < 7; i++) {
            battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield4.png")));
        }
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield1.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield2.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield3.png")));

    }
    /**
     * Loads the sprites for the different agent types.
     */
    private void loadAgentSprites() {
        // Sprites for Warriors
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_red_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_red_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_red_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_red_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_red_west_3.png"))
        ));

        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/warrior_blue_west_3.png"))
        ));

        // Sprites for Archers
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_red_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_red_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_red_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_red_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_red_west_3.png"))
        ));

        agentSprites.put(new AgentKey(Archer.class, false, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_blue_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_blue_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_blue_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/archer_blue_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/archer_blue_west_3.png"))
        ));

        // Sprites for Priests
        agentSprites.put(new AgentKey(Priest.class, true, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_red_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, true, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_red_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, true, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_red_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, true, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_red_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_red_west_3.png"))
        ));

        agentSprites.put(new AgentKey(Priest.class, false, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_blue_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, false, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_blue_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, false, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_blue_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Priest.class, false, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/priest_blue_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/priest_blue_west_3.png"))
        ));
        // Sprites for Gatherers
        agentSprites.put(new AgentKey(Gatherer.class, true, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, true, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, true, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, true, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_red_west_3.png"))
        ));

        agentSprites.put(new AgentKey(Gatherer.class, false, Orientation.NORTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_north_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_north_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_north_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, false, Orientation.SOUTH), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_south_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_south_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_south_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, false, Orientation.EAST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_east_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_east_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_east_3.png"))
        ));
        agentSprites.put(new AgentKey(Gatherer.class, false, Orientation.WEST), Arrays.asList(
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_west_1.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_west_2.png")),
                new ImageIcon(getClass().getResource("/sprites/gatherer_blue_west_3.png"))
        ));
    }
    /**
     * Loads the sprites for the different resource types.
     */
    private void loadResourceSprites() {
        princessSprites.put("blue", new ImageIcon(getClass().getResource("/sprites/princess_blue.png")));
        princessSprites.put("red", new ImageIcon(getClass().getResource("/sprites/princess_red.png")));
    }
    /**
     * Updates the animation frames for all agents.
     */
    private void updateAnimationFrames() {
        for (Agent agent : model.getAllAgents()) {
            int currentFrame = agentAnimationFrames.getOrDefault(agent, 0);
            agentAnimationFrames.put(agent, currentFrame + 1);
        }
        refreshBackground();
    }
    /**
     * Refreshes the background of the map, updating the icons for zones, agents, and resources.
     */
    private void refreshBackground() {
        Map<Vector2D, String> entitiesNamesToRender = new HashMap<>();

        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Vector2D position = Vector2D.of(x, y);
                JLabel cellLabel = cellsGrid.get(position);

                if (cellLabel != null) {
                    Cell cell = model.getCellByPosition(position);
                    Zone zoneType = cell.getZoneType();
                    ImageIcon zoneSprite;


                    // Handle animation for OUT_OF_MAP zone
                    if (zoneType == Zone.OUT_OF_MAP) {
                        boolean isBridge = cell.getStructure() instanceof Bridge;
                        Zone effectiveZoneType = isBridge ? Zone.OUT_OF_MAP : cell.getZoneType();

                        boolean hasBattlefieldN = y > 0 && (model.getCellByPosition(Vector2D.of(x, y - 1)).getStructure() instanceof Bridge
                                ? Zone.OUT_OF_MAP
                                : model.getCellByPosition(Vector2D.of(x, y - 1)).getZoneType()) == Zone.BATTLEFIELD;

                        boolean hasBattlefieldS = y < model.getHeight() - 1 && (model.getCellByPosition(Vector2D.of(x, y + 1)).getStructure() instanceof Bridge
                                ? Zone.OUT_OF_MAP
                                : model.getCellByPosition(Vector2D.of(x, y + 1)).getZoneType()) == Zone.BATTLEFIELD;

                        boolean hasBattlefieldE = x < model.getWidth() - 1 && (model.getCellByPosition(Vector2D.of(x + 1, y)).getStructure() instanceof Bridge
                                ? Zone.OUT_OF_MAP
                                : model.getCellByPosition(Vector2D.of(x + 1, y)).getZoneType()) == Zone.BATTLEFIELD;

                        boolean hasBattlefieldW = x > 0 && (model.getCellByPosition(Vector2D.of(x - 1, y)).getStructure() instanceof Bridge
                                ? Zone.OUT_OF_MAP
                                : model.getCellByPosition(Vector2D.of(x - 1, y)).getZoneType()) == Zone.BATTLEFIELD;

                        if (hasBattlefieldS && !hasBattlefieldN && !hasBattlefieldE && !hasBattlefieldW) zoneSprite = outOfMapSpritesS[outOfMapAnimationFrame];
                        else if (hasBattlefieldN && !hasBattlefieldS && !hasBattlefieldE && !hasBattlefieldW) zoneSprite = new ImageIcon(getClass().getResource("/sprites/river_s.png"));
                        else if (hasBattlefieldE && !hasBattlefieldS && !hasBattlefieldN && !hasBattlefieldW) zoneSprite = outOfMapSpritesE[outOfMapAnimationFrame];
                        else if (hasBattlefieldW && !hasBattlefieldS && !hasBattlefieldN && !hasBattlefieldE) zoneSprite = outOfMapSpritesW[outOfMapAnimationFrame];
                        else if (hasBattlefieldN && hasBattlefieldE) zoneSprite = new ImageIcon(getClass().getResource("/sprites/river_c_ne.png"));
                        else if (hasBattlefieldN && hasBattlefieldW) zoneSprite = new ImageIcon(getClass().getResource("/sprites/river_c_nw.png"));
                        else if (hasBattlefieldS && hasBattlefieldE) zoneSprite = outOfMapSpritesSE[outOfMapAnimationFrame];
                        else if (hasBattlefieldS && hasBattlefieldW) zoneSprite = outOfMapSpritesSW[outOfMapAnimationFrame];
                        else zoneSprite = outOfMapSprites[outOfMapAnimationFrame];
                    } else {
                        zoneSprite = zoneType == Zone.BATTLEFIELD
                                ? randomizedBattlefieldSprites.get(position)
                                : zoneSprites.get(zoneType);
                    }

                    ImageIcon combinedImage = new ImageIcon(createImageWithTransparency(null, zoneSprite.getImage()));

                    // Render structures, resources, and agents
                    MapStructure structure = cell.getStructure();
                    if (structure != null) {
                        ImageIcon structureIcon = getStructureIcon(structure);
                        if (structureIcon != null) {
                            combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, structureIcon.getImage()));
                        }

                        if ((structure instanceof Gate)) {
                            // Store the agent's name for the cell above (y-1)
                            Vector2D namePosition = Vector2D.of(x, y - 1);
                            if (y > 0) { // Ensure it's within bounds
                                entitiesNamesToRender.put(namePosition, structure.getName());
                            }
                        }
                    }

                    Resource resource = cell.getResource();
                    if (resource != null) {
                        if (resource instanceof Princess princess && !princess.isCarried()) {
                            String teamColor = princess.getTeam() ? "red" : "blue";
                            ImageIcon princessIcon = princessSprites.get(teamColor);
                            if (princessIcon != null) {
                                combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, princessIcon.getImage()));
                            }
                        } else {
                            ImageIcon resourceIcon = resourceSprites.get(resource.getClass());
                            if (resourceIcon != null) {
                                combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, resourceIcon.getImage()));
                            }
                        }

                        if ((resource instanceof Princess)) {
                            // Store the agent's name for the cell above (y-1)
                            Vector2D namePosition = Vector2D.of(x, y - 1);
                            if (y > 0) { // Ensure it's within bounds
                                entitiesNamesToRender.put(namePosition, resource.getName());
                            }
                        }
                    }

                    Agent agent = cell.getAgent();
                    if (agent != null && agent.getHp() > 0) {
                        ImageIcon agentIcon = getAgentIcon(agent);
                        if (agentIcon != null) {
                            combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, agentIcon.getImage()));
                        }

                        // Store the agent's name for the cell above (y-1)
                        Vector2D namePosition = Vector2D.of(x, y - 1);
                        if (y > 0) { // Ensure it's within bounds
                            entitiesNamesToRender.put(namePosition, agent.getName());
                        }
                    }

                    // Set the final combined image
                    cellLabel.setIcon(combinedImage);
                }
            }
        }

        // Overlay agent names on cells above
        for (Map.Entry<Vector2D, String> entry : entitiesNamesToRender.entrySet()) {
            JLabel nameLabel = cellsGrid.get(entry.getKey());
            if (nameLabel != null) {
                ImageIcon textImage = new ImageIcon(createImageWithText(entry.getValue()));
                nameLabel.setIcon(new ImageIcon(createImageWithTransparency((ImageIcon) nameLabel.getIcon(), textImage.getImage())));
            }
        }

        repaint();
    }
    /**
     * Gets the appropriate icon for a given structure.
     *
     * @param structure The structure instance.
     * @return The ImageIcon for the structure.
     */
    private ImageIcon getStructureIcon(MapStructure structure) {
        if (structure instanceof Gate gate) {
            boolean isDestroyed = gate.isDestroyed();
            boolean isRedTeam = gate.getTeam(); // 'True' means red team, 'false' means blue

            if (isDestroyed) {
                return new ImageIcon(getClass().getResource(isRedTeam
                        ? "/sprites/brokengate_r.png"
                        : "/sprites/brokengate_b.png"));
            } else {
                return new ImageIcon(getClass().getResource(isRedTeam
                        ? "/sprites/gate_r.png"
                        : "/sprites/gate_b.png"));
            }
        } else if (structure instanceof Wall) {
            return new ImageIcon(getClass().getResource("/sprites/wall.png"));
        } else if (structure instanceof Bridge) {
            return new ImageIcon(getClass().getResource("/sprites/bridge.png"));
        } else if (structure instanceof Tree tree) {
            if (tree.isDestroyed()) {
                return new ImageIcon(getClass().getResource("/sprites/cuttree.png"));
            } else {
                return new ImageIcon(getClass().getResource("/sprites/tree.png"));

            }
        }
        return null; // No structure icon if not recognized
    }
    /**
     * Gets the appropriate icon for a given agent, handling animation frames.
     *
     * @param agent The agent.
     * @return The ImageIcon for the agent, or null if not found.
     */
    private ImageIcon getAgentIcon(Agent agent) {
        Pose pose = agent.getPose();
        if (pose == null) return null;

        AgentKey key = new AgentKey(agent.getClass(), agent.getTeam(), pose.getOrientation());
        java.util.List<ImageIcon> sprites = agentSprites.get(key);
        if (sprites == null || sprites.isEmpty()) return null;

        int frame = agentAnimationFrames.getOrDefault(agent, 0);
        ImageIcon baseIcon = sprites.get(frame % sprites.size());

        // If the agent is carrying an item, add overlay
        Image finalImage = (agent.getCarriedItem() != null)
                ? createImageWithTransparency(baseIcon, crownIcon.getImage()) // Apply overlay
                : baseIcon.getImage();

        return new ImageIcon(finalImage);
    }
    /**
     * Updates the agent list display.
     */
    private void updateAgentList() {
        agentStatusContainer.removeAll(); // Clear the container

        // Sort agents: first by team (false -> blue, true -> red), then alphabetically
        List<Agent> sortedAgents = model.getAllAgents().stream()
                .sorted(Comparator.comparing(Agent::getTeam)
                        .thenComparing(Agent::getName))
                .collect(Collectors.toList());

        // Separate teams into two rows
        JPanel blueTeamRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel redTeamRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        for (Agent agent : sortedAgents) {
            // Create a panel to contain the agent's information
            JPanel agentPanel = new JPanel();
            agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.Y_AXIS)); // Vertical layout
            agentPanel.setPreferredSize(new Dimension(160, 120)); // Fixed size for uniformity
            agentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            agentPanel.setBackground(new Color(230, 230, 230)); // Light gray background

            // Create a label for the agent's name
            JLabel nameLabel = new JLabel(agent.getName(), SwingConstants.CENTER);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Position Label
            JLabel positionLabel = new JLabel("Pos: " + agent.getPose().getPosition(), SwingConstants.CENTER);
            positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create HP bar
            JProgressBar hpBar = new JProgressBar(0, agent.getMaxHp()); // Max HP as upper bound
            hpBar.setValue(agent.getHp()); // Current HP
            hpBar.setStringPainted(true); // Show HP value as text
            hpBar.setForeground(agent.getHp() > (agent.getMaxHp() / 2) ? Color.GREEN : Color.RED); // Green if HP > 50%, red otherwise
            hpBar.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create a label for the agent's state
            JLabel stateLabel = new JLabel("State: " + agent.getState(), SwingConstants.CENTER);
            stateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Set agent icon
            ImageIcon agentIcon = getAgentIcon(agent);
            JLabel iconLabel = new JLabel();
            if (agentIcon != null) {
                iconLabel.setIcon(agentIcon);
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add all components to the agent panel
            agentPanel.add(iconLabel);
            agentPanel.add(nameLabel);
            agentPanel.add(positionLabel);
            agentPanel.add(hpBar);
            agentPanel.add(stateLabel);

            // Add agent panel to the appropriate team row
            if (agent.getTeam()) { // Red team
                redTeamRow.add(agentPanel);
            } else { // Blue team
                blueTeamRow.add(agentPanel);
            }
        }

        // Add the two team rows to the main container
        agentStatusContainer.setLayout(new BoxLayout(agentStatusContainer, BoxLayout.Y_AXIS));
        agentStatusContainer.add(new JLabel("Blue Team:"));
        agentStatusContainer.add(blueTeamRow);
        agentStatusContainer.add(new JLabel("Red Team:"));
        agentStatusContainer.add(redTeamRow);

        agentStatusContainer.revalidate();
        agentStatusContainer.repaint();
    }
    /**
     * Creates and initializes the grid panel representing the game map.
     *
     * @return The JPanel representing the grid.
     */
    private JPanel createGridPanel() {
        JPanel grid = new JPanel(new GridLayout(model.getHeight(), model.getWidth()));
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                JLabel cellLabel = new JLabel();
                cellLabel.setHorizontalAlignment(SwingConstants.CENTER);
                cellLabel.setVerticalAlignment(SwingConstants.CENTER);
                cellsGrid.put(Vector2D.of(x, y), cellLabel);
                cellLabel.setPreferredSize(new Dimension(32, 32));
                grid.add(cellLabel);

                // Randomize battlefield sprites
                Cell cell = model.getCellByPosition(Vector2D.of(x, y));
                if (cell.getZoneType() == Zone.BATTLEFIELD) {
                    ImageIcon randomSprite = battlefieldSprites.get(new Random().nextInt(battlefieldSprites.size()));
                    randomizedBattlefieldSprites.put(Vector2D.of(x, y), randomSprite);
                }
            }
        }
        return grid;
    }
    /**
     * Creates and initializes the panel displaying agent statuses.
     *
     * @return The JPanel representing the agent status panel.
     */
    private JPanel createAgentStatusPanel() {
        int fixedHeight = 350;

        // Main panel with a fixed height and variable width
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Agent Status"));
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, fixedHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight));

        // Container for agent statuses (will scroll if too many agents)
        JPanel agentStatusContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 40));

        // Wrap it inside a JScrollPane
        JScrollPane scrollPane = new JScrollPane(agentStatusContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(panel.getPreferredSize().width, fixedHeight - 20)); // Adjust for title bar

        panel.add(scrollPane);

        // Save the container for dynamic updates
        this.agentStatusContainer = agentStatusContainer;

        return panel;
    }
    /**
     * Creates and initializes the panel displaying resource information (wood, princesses, gate HP).
     *
     * @return The JPanel representing the resource panel.
     */
    private JPanel createResourcePanel() {
        int resourceWidth = 115;
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                int halfHeight = getHeight() / 2;

                // Define colors (lighter blue and red)
                Color lightBlue = new Color(173, 216, 230); // Light Sky Blue
                Color lightRed = new Color(255, 182, 193); // Light Pink

                // Fill top half with light blue
                g2d.setColor(lightBlue);
                g2d.fillRect(0, 0, getWidth(), halfHeight);

                // Fill bottom half with light red
                g2d.setColor(lightRed);
                g2d.fillRect(0, halfHeight, getWidth(), getHeight());
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Game Infos"));
        panel.setPreferredSize(new Dimension(resourceWidth, panel.getPreferredSize().height));
        panel.setMaximumSize(new Dimension(resourceWidth, Integer.MAX_VALUE - 2));

        // Load icons
        ImageIcon woodBlueIcon = new ImageIcon(getClass().getResource("/sprites/wood_blue.png"));
        ImageIcon woodRedIcon = new ImageIcon(getClass().getResource("/sprites/wood_red.png"));
        ImageIcon princessBlueIcon = princessSprites.get("blue");
        ImageIcon princessRedIcon = princessSprites.get("red");


        // Create labels for icons
        JLabel woodBlueImage = new JLabel(woodBlueIcon);
        JLabel woodRedImage = new JLabel(woodRedIcon);
        JLabel princessBlueImage = new JLabel(princessBlueIcon);
        JLabel princessRedImage = new JLabel(princessRedIcon);

        // Create fixed text labels with custom colors
        JLabel blueTeamLabel = new JLabel("Blue Team");
        blueTeamLabel.setForeground(Color.BLUE);
        blueTeamLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel redTeamLabel = new JLabel("Red Team");
        redTeamLabel.setForeground(Color.RED);
        redTeamLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Create labels for values
        woodBlueLabel = new JLabel("0");
        woodBlueLabel.setForeground(Color.BLUE);
        woodRedLabel = new JLabel("0");
        woodRedLabel.setForeground(Color.RED);
        princessBlueLabel = new JLabel("On ground");
        //princessRedSpawnLabel = new JLabel("Spawn Point");
        princessBlueLabel.setForeground(Color.BLUE);
        princessRedLabel = new JLabel("On ground");
        princessRedLabel.setForeground(Color.RED);

        // Center text below icons
        woodBlueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        woodRedLabel.setHorizontalAlignment(SwingConstants.LEFT);
        princessBlueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        princessRedLabel.setHorizontalAlignment(SwingConstants.LEFT);

        gateB1HpBar = createHpBar();
        gateB2HpBar = createHpBar();
        gateR1HpBar = createHpBar();
        gateR2HpBar = createHpBar();

        // Add components with spacing
        panel.add(Box.createVerticalStrut(7));
        panel.add(createLeftAlignedPanel(blueTeamLabel));
        panel.add(Box.createVerticalStrut(7));
        panel.add(createLeftAlignedPanel(princessBlueImage));
        panel.add(createLeftAlignedPanel(princessBlueLabel));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createLeftAlignedPanel(woodBlueImage));
        panel.add(createLeftAlignedPanel(woodBlueLabel));

        panel.add(Box.createVerticalStrut(5));
        panel.add(createLabeledHpBar("G_B1", gateB1HpBar));
        panel.add(Box.createVerticalStrut(7));

        panel.add(createLabeledHpBar("G_B2", gateB2HpBar));
        panel.add(Box.createVerticalGlue());

        panel.add(Box.createVerticalStrut(20));

        panel.add(createLabeledHpBar("G_R1", gateR1HpBar));
        panel.add(Box.createVerticalStrut(7));

        panel.add(createLabeledHpBar("G_R2", gateR2HpBar));
        panel.add(Box.createVerticalStrut(5));

        panel.add(createLeftAlignedPanel(woodRedImage));
        panel.add(createLeftAlignedPanel(woodRedLabel));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createLeftAlignedPanel(princessRedImage));
        panel.add(createLeftAlignedPanel(princessRedLabel));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createLeftAlignedPanel(redTeamLabel));

        return panel;
    }
    /**
     * Updates the resource panel with the latest information from the game model.
     */
    private void updateResourcePanel() {
        woodBlueLabel.setText(String.valueOf(model.getWoodAmountBlue().get()));
        woodRedLabel.setText(String.valueOf(model.getWoodAmountRed().get()));

        if (model.getPrincessByName("princess_b").get().isCarried()) {
            princessBlueLabel.setText("Picked Up");
        } else {
            princessBlueLabel.setText("On ground");
        }

        if (model.getPrincessByName("princess_r").get().isCarried()) {
            princessRedLabel.setText("Picked Up");
        } else {
            princessRedLabel.setText("On ground");
        }

        updateGateStatus("gate_b1", gateB1HpBar);
        updateGateStatus("gate_b2", gateB2HpBar);
        updateGateStatus("gate_r1", gateR1HpBar);
        updateGateStatus("gate_r2", gateR2HpBar);
    }
    /**
     * Updates the HP bar of a specific gate.
     *
     * @param gateName The name of the gate.
     * @param hpBar    The JProgressBar representing the gate's HP.
     */
    private void updateGateStatus(String gateName, JProgressBar hpBar) {
        Gate gate = model.getGateByName(gateName).get();
        hpBar.setMaximum(gate.getMaxHp());
        hpBar.setValue(gate.getHp());
        hpBar.setForeground(gate.getHp() > (gate.getMaxHp() / 2) ? Color.GREEN : Color.RED);
    }
    /**
     * Creates a JProgressBar for displaying HP.
     *
     * @return The created JProgressBar.
     */
    private JProgressBar createHpBar() {
        JProgressBar hpBar = new JProgressBar(0, 100); // Assuming 100 is the max HP
        hpBar.setValue(100);
        hpBar.setStringPainted(true);
        hpBar.setForeground(Color.GREEN);
        hpBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        return hpBar;
    }
    /**
     * Creates a panel containing a label and an HP bar.
     *
     * @param label The text for the label.
     * @param hpBar The JProgressBar to display.
     * @return The JPanel containing the label and HP bar.
     */
    private JPanel createLabeledHpBar(String label, JProgressBar hpBar) {
        Color lightBlue = new Color(173, 216, 230); // Light Sky Blue
        Color lightRed = new Color(255, 182, 193); // Light Pink

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel gateLabel = new JLabel(label);

        if (label.contains(""+'B')) {
            panel.setBackground(lightBlue);
            gateLabel.setForeground(Color.BLUE);
        } else if (label.contains(""+'R')) {
            panel.setBackground(lightRed);
            gateLabel.setForeground(Color.RED);
        }

        panel.add(gateLabel);
        panel.add(Box.createHorizontalStrut(5)); // Small spacing
        panel.add(hpBar);
        return panel;
    }
    /**
     * Creates a panel with a left-aligned label.
     *
     * @param label The JLabel to be left-aligned.
     * @return The JPanel containing the left-aligned label.
     */
    private JPanel createLeftAlignedPanel(JLabel label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false); // Keep background transparency
        panel.add(label);
        return panel;
    }
    /**
     * Custom renderer for agent statuses in the JList.
     */
    private class AgentStatusRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Agent) {
                Agent agent = (Agent) value;

                // Display agent name, HP, and position
                label.setText(String.format("%s: HP=%d, Pos=%s, St=%s", agent.getName(), agent.getHp(), agent.getPose().getPosition(), agent.getState()));

                // Set the agent's icon
                ImageIcon agentIcon = getAgentIcon(agent);
                if (agentIcon != null) {
                    label.setIcon(agentIcon);
                }
            }
            return label;
        }
    }
    /**
     * Creates an image with the agent's, gate's or princess name as text.
     *
     * @param agentName The name of the agent.
     * @return The Image containing the agent's name.
     */
    private Image createImageWithText(String agentName) {
        int width = 50, height = 20; // Size of the text box
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw background rectangle (kept semi-transparent for readability)
        g2d.setColor(new Color(50, 50, 50, 180)); // Dark semi-transparent box
        g2d.fillRoundRect(0, 5, width, height, 5, 5);

        // Convert agent name to the desired format: first letter of the first word and last part of the string
        String[] parts = agentName.split("_");
        String formattedName = parts[0].substring(0, 1).toUpperCase() + "_" + parts[1].toUpperCase();

        // Draw text inside the box
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.setColor(Color.WHITE);
        FontMetrics metrics = g2d.getFontMetrics();
        int textX = 2;
        int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent() + 3;
        g2d.drawString(formattedName, textX, textY);

        g2d.dispose();
        return bufferedImage;
    }
    /**
     * Creates a new image by combining a base image and an overlay image with transparency.
     *
     * @param baseImage    The base image (can be null).
     * @param overlayImage The overlay image.
     * @return The combined Image.
     */
    private Image createImageWithTransparency(ImageIcon baseImage, Image overlayImage) {
        Image background = baseImage != null ? baseImage.getImage() : null;
        int width = background != null ? background.getWidth(null) : overlayImage.getWidth(null);
        int height = background != null ? background.getHeight(null) : overlayImage.getHeight(null);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw the background (zone or previous layer)
        if (background != null) {
            g2d.drawImage(background, 0, 0, null);
        }

        // Draw the overlay (structure or agent)
        g2d.drawImage(overlayImage, 0, 0, null);

        g2d.dispose();
        return bufferedImage;
    }
    /**
     * Displays a temporary effect (e.g., attack, damage, heal) at a specific position.
     *
     * @param position    The position where the effect should be displayed.
     * @param spritePath  The path to the effect sprite.
     * @param duration    The duration of the effect in milliseconds.
     */
    public void showTemporaryEffect(Vector2D position, String spritePath, int duration) {
        JLabel cellLabel = cellsGrid.get(position);

        if (cellLabel != null) {
            ImageIcon effectSprite = new ImageIcon(getClass().getResource(spritePath));
            ImageIcon previousIcon = (ImageIcon) cellLabel.getIcon(); // Save the current icon

            Image combinedImage = createImageWithTransparency(previousIcon, effectSprite.getImage());

            cellLabel.setIcon(new ImageIcon(combinedImage)); // Set the blended effect
            cellLabel.revalidate();
            cellLabel.repaint();

            // Timer to remove the effect after 'duration' milliseconds
            javax.swing.Timer timer = new javax.swing.Timer(duration, e -> {
                cellLabel.setIcon(previousIcon); // Restore the previous state
                cellLabel.revalidate();
                cellLabel.repaint();
            });

            timer.setRepeats(false); // Ensure it runs only once
            timer.start();
        }
    }
    /**
     * Triggers the attack view effect at a given position.
     *
     * @param position The position of the attack.
     */
    public void triggerAttackView(Vector2D position) {
        showTemporaryEffect(position, "/sprites/attack.png", 250);
    }
    /**
     * Triggers the damage view effect at a given position.
     *
     * @param position The position where damage was taken.
     */
    public void triggerDamageView(Vector2D position) {
        showTemporaryEffect(position, "/sprites/damage.png", 250);
    }
    /**
     * Triggers the heal view effect at a given position.
     *
     * @param position The position where healing occurred.
     */
    public void triggerHealView(Vector2D position) {
        showTemporaryEffect(position, "/sprites/heal.png", 250);
    }
    /**
     * Notifies the view that the model has changed, triggering updates to the UI.
     */
    @Override
    public void notifyModelChanged() {
        SwingUtilities.invokeLater(() -> {
            refreshBackground();
            updateAgentList();
            updateResourcePanel(); // New method to refresh wood count
        });
    }
}
