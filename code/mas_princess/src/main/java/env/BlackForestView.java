package env;

import env.utils.*;
import env.objects.structures.*;
import env.agents.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class BlackForestView extends JFrame implements MapView {

    private final MapModel model; // The map model
    private final Map<Vector2D, JLabel> cellsGrid = new HashMap<>(); // Mapping of positions to grid labels
    private final Map<Zone, ImageIcon> zoneSprites = new EnumMap<>(Zone.class); // Mapping of zone types to sprites
    private final Map<AgentKey, ImageIcon> agentSprites = new HashMap<>();
    private final java.util.List<ImageIcon> battlefieldSprites = new ArrayList<>(); // List of possible battlefield sprites
    private final Map<Vector2D, ImageIcon> randomizedBattlefieldSprites = new HashMap<>(); // Randomized battlefield sprites
    private final DefaultListModel<Agent> agentListModel = new DefaultListModel<>(); // Model for the agent list
    private final JList<Agent> agentList = new JList<>(agentListModel); // List to display agents
    private JPanel agentStatusContainer; // Panel to display agent statuses



    // Constructor
    public BlackForestView(MapModel model) {
        this.model = Objects.requireNonNull(model);

        // Load sprites for zones and agents
        loadZoneSprites();
        loadAgentSprites();

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
        JPanel agentStatusContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane scrollPane = new JScrollPane(agentStatusContainer);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Save the container for dynamic updates
        this.agentStatusContainer = agentStatusContainer;

        return panel;
    }


    // Update the agent list dynamically
    private void updateAgentList() {
        agentStatusContainer.removeAll(); // Clear the container

        for (Agent agent : model.getAllAgents()) {
            // Create a label for each agent
            JLabel agentLabel = new JLabel(String.format("<html>%s<br>HP: %d<br>Pos: %s</html>",
                    agent.getName(), agent.getHp(), agent.getPose().getPosition()));

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
                label.setText(String.format("%s: HP=%d, Pos=%s", agent.getName(), agent.getHp(), agent.getPose().getPosition()));

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
        zoneSprites.put(Zone.OUT_OF_MAP, new ImageIcon(getClass().getResource("/sprites/river.png")));

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
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.NORTH), new ImageIcon(getClass().getResource("/sprites/warrior_red_north.png")));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.SOUTH), new ImageIcon(getClass().getResource("/sprites/warrior_red_south.png")));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.EAST), new ImageIcon(getClass().getResource("/sprites/warrior_red_east.png")));
        agentSprites.put(new AgentKey(Warrior.class, true, Orientation.WEST), new ImageIcon(getClass().getResource("/sprites/warrior_red_west.png")));

        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.NORTH), new ImageIcon(getClass().getResource("/sprites/warrior_blue_north.png")));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.SOUTH), new ImageIcon(getClass().getResource("/sprites/warrior_blue_south.png")));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.EAST), new ImageIcon(getClass().getResource("/sprites/warrior_blue_east.png")));
        agentSprites.put(new AgentKey(Warrior.class, false, Orientation.WEST), new ImageIcon(getClass().getResource("/sprites/warrior_blue_west.png")));

        // Sprites for Archers
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.NORTH), new ImageIcon(getClass().getResource("/sprites/archer_red_north.png")));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.SOUTH), new ImageIcon(getClass().getResource("/sprites/archer_red_south.png")));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.EAST), new ImageIcon(getClass().getResource("/sprites/archer_red_east.png")));
        agentSprites.put(new AgentKey(Archer.class, true, Orientation.WEST), new ImageIcon(getClass().getResource("/sprites/archer_red_west.png")));

        agentSprites.put(new AgentKey(Archer.class, false, Orientation.NORTH), new ImageIcon(getClass().getResource("/sprites/archer_blue_north.png")));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.SOUTH), new ImageIcon(getClass().getResource("/sprites/archer_blue_south.png")));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.EAST), new ImageIcon(getClass().getResource("/sprites/archer_blue_east.png")));
        agentSprites.put(new AgentKey(Archer.class, false, Orientation.WEST), new ImageIcon(getClass().getResource("/sprites/archer_blue_west.png")));
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

                    // Get the zone sprite
                    ImageIcon zoneSprite = zoneType == Zone.BATTLEFIELD
                            ? randomizedBattlefieldSprites.get(position)
                            : zoneSprites.get(zoneType);
                    ImageIcon combinedImage = new ImageIcon(createImageWithTransparency(null, zoneSprite.getImage()));

                    // Render structures if they exist
                    MapStructure structure = cell.getStructure();
                    if (structure != null) {
                        ImageIcon structureIcon = getStructureIcon(structure);
                        if (structureIcon != null) {
                            combinedImage = new ImageIcon(createImageWithTransparency(combinedImage, structureIcon.getImage()));
                        }
                    }

                    // Render agents if they exist
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
            return new ImageIcon(getClass().getResource("/sprites/gate.png"));
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
        if (pose == null) {
            return null; // No pose means no icon
        }
        // Retrieve sprite based on agent class, team, and orientation
        return agentSprites.get(new AgentKey(agent.getClass(), agent.getTeam(), pose.getOrientation()));
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
