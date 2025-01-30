package env;

import env.utils.*;
import env.objects.structures.*;
import env.objects.resources.*;
import env.agents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

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

    private int outOfMapAnimationFrame = 0; // Track animation frame for OUT_OF_MAP zone
    private final ImageIcon[] outOfMapSprites = new ImageIcon[2]; // Hold river sprites for animation
    private final javax.swing.Timer outOfMapAnimationTimer = new javax.swing.Timer(500, e -> {
        outOfMapAnimationFrame = (outOfMapAnimationFrame + 1) % 2; // Toggle between 0 and 1
        refreshBackground(); // Refresh map with the new frame
    });

    private final ImageIcon crownIcon = new ImageIcon(getClass().getResource("/sprites/crown.png"));



    // Constructor
    public BlackForestView(MapModel model) {
        this.model = Objects.requireNonNull(model);

        // Load sprites for zones and agents
        loadZoneSprites();
        loadAgentSprites();
        loadResourceSprites();
        animationTimer.start();
        outOfMapAnimationTimer.start();

        // Set up the main container
        JPanel contentPane = new JPanel(new BorderLayout());

        // Create and populate the map grid
        JPanel gridPanel = createGridPanel();

        // Create the agent status panel
        JPanel agentStatusPanel = createAgentStatusPanel();

        // Add panels to the main container
        contentPane.add(gridPanel, BorderLayout.CENTER); // Map grid in the center
        contentPane.add(agentStatusPanel, BorderLayout.SOUTH); // Agent statuses at the bottom

        setContentPane(contentPane);
        pack();

        setResizable(false);

        // Refresh the view initially
        refreshBackground();
        updateAgentList();
    }

    // Create the grid panel for the map
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

    // Create the agent status panel
    private JPanel createAgentStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Agent Status"));

        // Use WrapLayout for the agent container
        JPanel agentStatusContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 40));
        panel.add(agentStatusContainer, BorderLayout.CENTER); // Add the container directly

        // Save the container for dynamic updates
        this.agentStatusContainer = agentStatusContainer;

        return panel;
    }


    // Update the agent list dynamically
    private void updateAgentList() {
        agentStatusContainer.removeAll(); // Clear the container

        for (Agent agent : model.getAllAgents()) {
            // Create a label for each agent
            JLabel agentLabel = new JLabel();
            if (agent.getCarriedItem() == null) {
                agentLabel = new JLabel(String.format("<html>%s<br>HP: %d<br>Pos: %s<br>St: %s</html>",
                        agent.getName(), agent.getHp(), agent.getPose().getPosition(), agent.getState()));
            } else {
                agentLabel = new JLabel(String.format("<html>%s<br>HP: %d<br>Pos: %s<br>St: %s</html>",
                        agent.getName(), agent.getHp(), agent.getPose().getPosition(), agent.getState()));
            }

            // Set agent icon
            ImageIcon agentIcon = getAgentIcon(agent);
            if (agentIcon != null) {
                agentLabel.setIcon(agentIcon);
                agentLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                agentLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
            }

            // Add to the container
            agentStatusContainer.add(agentLabel);
        }

        agentStatusContainer.revalidate();
        agentStatusContainer.repaint();
    }


    // Custom renderer for agent statuses
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

    // Load sprites for each zone type
    private void loadZoneSprites() {
        zoneSprites.put(Zone.BBASE, new ImageIcon(getClass().getResource("/sprites/bbase.png")));
        zoneSprites.put(Zone.RBASE, new ImageIcon(getClass().getResource("/sprites/rbase.png")));
        //zoneSprites.put(Zone.OUT_OF_MAP, new ImageIcon(getClass().getResource("/sprites/river.png")));
        // Load animated sprites for OUT_OF_MAP
        outOfMapSprites[0] = new ImageIcon(getClass().getResource("/sprites/river1.png"));
        outOfMapSprites[1] = new ImageIcon(getClass().getResource("/sprites/river2.png"));

    // Add multiple battlefield sprites
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield1.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield2.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield3.png")));
        battlefieldSprites.add(new ImageIcon(getClass().getResource("/sprites/battlefield4.png")));
    }

    // Load sprites for each agent type
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


    private void loadResourceSprites() {
        princessSprites.put("blue", new ImageIcon(getClass().getResource("/sprites/princess_blue.png")));
        princessSprites.put("red", new ImageIcon(getClass().getResource("/sprites/princess_red.png")));
    }

    // Refresh the view to display sprites based on zone types, structures, and agents
    private void refreshBackground() {
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
                        zoneSprite = outOfMapSprites[outOfMapAnimationFrame];
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
                    }

                    Agent agent = cell.getAgent();
                    if (agent != null) {
                        ImageIcon agentIcon = getAgentIcon(agent);
                        if (agentIcon != null) {
                            combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, agentIcon.getImage()));
                        }
                    }

                    // Set the final combined image
                    cellLabel.setIcon(combinedImage);
                }
            }
        }
        repaint();
    }


    // Get the appropriate structure icon
    private ImageIcon getStructureIcon(MapStructure structure) {
        if (structure instanceof Gate) {
            if (!((Gate) structure).isDestroyed()){
                return new ImageIcon(getClass().getResource("/sprites/gate.png"));
            } else {
                return new ImageIcon(getClass().getResource("/sprites/brokengate.png"));
            }
        } else if (structure instanceof Wall) {
            return new ImageIcon(getClass().getResource("/sprites/wall.png"));
        } else if (structure instanceof Bridge) {
            return new ImageIcon(getClass().getResource("/sprites/bridge.png"));
        } else if (structure instanceof Tree) {
            return new ImageIcon(getClass().getResource("/sprites/tree.png"));
        }
        return null; // No structure icon if not recognized
    }

    // Get the appropriate agent icon
    private ImageIcon getAgentIcon(Agent agent) {
        Pose pose = agent.getPose();
        if (pose == null) return null;

        AgentKey key = new AgentKey(agent.getClass(), agent.getTeam(), pose.getOrientation());
        java.util.List<ImageIcon> sprites = agentSprites.get(key);
        if (sprites == null || sprites.isEmpty()) return null;

        int frame = agentAnimationFrames.getOrDefault(agent, 0);
        ImageIcon baseIcon = sprites.get(frame % sprites.size());

        if (agent.getCarriedItem() != null) {
            return new ImageIcon(createImageWithTransparency(baseIcon, crownIcon.getImage()));
        }
        return baseIcon;
    }


        // Create a new image by combining existing image and overlay icon
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
    private void updateAnimationFrames() {
        for (Agent agent : model.getAllAgents()) {
            int currentFrame = agentAnimationFrames.getOrDefault(agent, 0);
            agentAnimationFrames.put(agent, currentFrame + 1);
        }
        refreshBackground();
    }

    public void showTemporaryEffect(Vector2D position, String spritePath, int duration) {
        JLabel cellLabel = cellsGrid.get(position);

        if (cellLabel != null) {
            ImageIcon effectSprite = new ImageIcon(getClass().getResource(spritePath));
            ImageIcon previousIcon = (ImageIcon) cellLabel.getIcon(); // Save the current icon

            cellLabel.setIcon(effectSprite); // Set the temporary effect
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


    @Override
    public void notifyModelChanged() {
        SwingUtilities.invokeLater(() -> {
            refreshBackground();
            updateAgentList();
        });
    }

    @Override
    public MapModel getModel() {
        return model;
    }
}
