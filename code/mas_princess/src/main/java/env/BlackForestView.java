package env;

import env.utils.*;
import env.objects.structures.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlackForestView extends JFrame implements MapView {

    private final MapModel model; // The map model
    private final Map<Vector2D, JLabel> cellsGrid = new HashMap<>(); // Mapping of positions to grid labels
    private final Map<Zone, ImageIcon> zoneSprites = new EnumMap<>(Zone.class); // Mapping of zone types to sprites

    // Constructor
    public BlackForestView(MapModel model) {
        this.model = Objects.requireNonNull(model);

        // Load sprites for each zone
        loadZoneSprites();

        // Setting up the main container
        JPanel contentPane = new JPanel(new BorderLayout());
        JPanel grid = new JPanel(new GridLayout(model.getHeight(), model.getWidth()));

        // Initialize grid cells
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                JLabel cellLabel = new JLabel();
                cellLabel.setHorizontalAlignment(SwingConstants.CENTER);
                cellLabel.setVerticalAlignment(SwingConstants.CENTER);
                cellsGrid.put(Vector2D.of(x, y), cellLabel);
                cellLabel.setPreferredSize(new Dimension(16, 16));
                grid.add(cellLabel);
            }
        }

        contentPane.add(grid, BorderLayout.CENTER);
        setContentPane(contentPane);
        pack();

        // Refresh the background view to show the sprites initially
        refreshBackground();
    }

    // Load sprites for each zone type
    private void loadZoneSprites() {
        zoneSprites.put(Zone.BBASE, new ImageIcon(getClass().getResource("/sprites/bbase.png")));
        zoneSprites.put(Zone.RBASE, new ImageIcon(getClass().getResource("/sprites/rbase.png")));
        zoneSprites.put(Zone.BATTLEFIELD, new ImageIcon(getClass().getResource("/sprites/battlefield.png")));
        zoneSprites.put(Zone.OUT_OF_MAP, new ImageIcon(getClass().getResource("/sprites/river.png")));
    }

    // Refresh the background to display sprites based on zone types and structures
    private void refreshBackground() {
        for (int y = 0; y < model.getHeight(); y++) {
            for (int x = 0; x < model.getWidth(); x++) {
                Vector2D position = Vector2D.of(x, y);
                JLabel cellLabel = cellsGrid.get(position);

                if (cellLabel != null) {
                    Cell cell = model.getCellByPosition(position);
                    Zone zoneType = cell.getZoneType();
                    ImageIcon zoneSprite = zoneSprites.get(zoneType);
                    cellLabel.setIcon(zoneSprite); // Set the background sprite for the zone

                    // Now render structures if they exist
                    MapStructure structure = cell.getStructure();
                    if (structure != null) {
                        ImageIcon structureIcon = getStructureIcon(structure);
                        // Check if structureIcon is not null and then set it
                        if (structureIcon != null) {
                            // Make sure the transparency is preserved
                            cellLabel.setIcon(new ImageIcon(createImageWithTransparency(cellLabel, structureIcon.getImage())));
                        }
                    }
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
        }
        return null; // No structure icon if not recognized
    }

    // This method creates a new image by combining the existing image (with transparency) and the structure icon.
    private Image createImageWithTransparency(JLabel cellLabel, Image structureImage) {
        Image background = ((ImageIcon) cellLabel.getIcon()).getImage();
        int width = background.getWidth(null);
        int height = background.getHeight(null);

        // Create a new image with transparency
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // First draw the background (zone)
        g2d.drawImage(background, 0, 0, null);

        // Now draw the structure image on top
        g2d.drawImage(structureImage, 0, 0, null);

        g2d.dispose();

        return bufferedImage;
    }

    @Override
    public void notifyModelChanged() {
        SwingUtilities.invokeLater(this::refreshBackground);
    }

    @Override
    public MapModel getModel() {
        return model;
    }
}
