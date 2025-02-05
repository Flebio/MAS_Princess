package env;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.Properties;

public class ConfigWindow extends JFrame {
    private JTextField widthField, heightField;
    private JComboBox<Integer> gathererBlue, archerBlue, warriorBlue, priestBlue;
    private JComboBox<Integer> gathererRed, archerRed, warriorRed, priestRed;
    private JButton startGameButton;
    // Get the root project directory (code/)
    static File projectRoot = new File(System.getProperty("user.dir")).getParentFile();

    // Define paths dynamically
    File mas2jFile = new File(projectRoot, "mas_princess/mas_princess.mas2j");
    File spriteDir = new File(projectRoot, "mas_princess/src/main/resources/sprites/");

    private final String SPRITE_PATH = spriteDir.getAbsolutePath() + File.separator;
    private final String MAS2J_FILE_PATH = mas2jFile.getAbsolutePath();
    private final String LOGO_PATH = SPRITE_PATH + "logo.png";
    private final File configFile = new File(projectRoot, "config.properties");
    private static Process gameProcess; // Store the process




    public ConfigWindow() {
        setTitle("Game Configuration");
        setSize(800, 700);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Panel (uses BorderLayout)
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Logo Panel
        JLabel logoLabel = loadImage(LOGO_PATH, 550, 200);
        if (logoLabel != null) {
            JPanel logoPanel = new JPanel();
            logoPanel.add(logoLabel);
            mainPanel.add(logoPanel, BorderLayout.NORTH);
        }

        // Create a Center Panel to hold dimensions and team composition
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Dimensions Panel
        JPanel dimensionsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        dimensionsPanel.setBorder(BorderFactory.createTitledBorder("Map Dimensions"));

        dimensionsPanel.add(new JLabel("Map Width:"));
        widthField = new JTextField("40");
        widthField.setPreferredSize(new Dimension(50, 20)); // Reduce height
        dimensionsPanel.add(widthField);

        dimensionsPanel.add(new JLabel("Map Height:"));
        heightField = new JTextField("15");
        heightField.setPreferredSize(new Dimension(50, 20)); // Reduce height
        dimensionsPanel.add(heightField);

        centerPanel.add(dimensionsPanel, BorderLayout.NORTH); // Place it above the team settings

        // Team Composition Panel
        JPanel teamPanel = new JPanel(new GridLayout(5, 5, 10, 10));
        teamPanel.setBorder(BorderFactory.createTitledBorder("Team Compositions"));

        // First row - Headers
        teamPanel.add(new JLabel("Agent Type", SwingConstants.CENTER));
        teamPanel.add(new JLabel(""));
        teamPanel.add(new JLabel("Blue Team", SwingConstants.CENTER));
        teamPanel.add(new JLabel(""));
        teamPanel.add(new JLabel("Red Team", SwingConstants.CENTER));

        String[][] spriteFiles = {
                {"gatherer_blue_south_2.png", "gatherer_red_south_2.png"},
                {"archer_blue_south_2.png", "archer_red_south_2.png"},
                {"warrior_blue_south_2.png", "warrior_red_south_2.png"},
                {"priest_blue_south_2.png", "priest_red_south_2.png"}
        };

        String[] agentNames = {"Gatherer", "Archer", "Warrior", "Priest"};
        gathererBlue = createAgentDropdown(2); // Limit Gatherer max 1
        gathererRed = createAgentDropdown(2);
        archerBlue = createAgentDropdown(4);
        archerRed = createAgentDropdown(4);
        warriorBlue = createAgentDropdown(4);
        warriorRed = createAgentDropdown(4);
        priestBlue = createAgentDropdown(4);
        priestRed = createAgentDropdown(4);

        JComboBox<Integer>[] blueTeamDropdowns = new JComboBox[]{gathererBlue, archerBlue, warriorBlue, priestBlue};
        JComboBox<Integer>[] redTeamDropdowns = new JComboBox[]{gathererRed, archerRed, warriorRed, priestRed};

        addAgentSelectionListener(blueTeamDropdowns, true);
        addAgentSelectionListener(redTeamDropdowns, false);

        // Agent rows
        for (int i = 0; i < agentNames.length; i++) {
            JLabel blueSpriteLabel = loadImage(SPRITE_PATH + spriteFiles[i][0], 40, 40);
            JLabel redSpriteLabel = loadImage(SPRITE_PATH + spriteFiles[i][1], 40, 40);

            teamPanel.add(new JLabel(agentNames[i], SwingConstants.CENTER)); // Agent Type
            teamPanel.add(blueSpriteLabel != null ? blueSpriteLabel : new JLabel("N/A")); // Blue Sprite
            teamPanel.add(blueTeamDropdowns[i]); // Blue Dropdown
            teamPanel.add(redSpriteLabel != null ? redSpriteLabel : new JLabel("N/A")); // Red Sprite
            teamPanel.add(redTeamDropdowns[i]); // Red Dropdown
        }

        centerPanel.add(teamPanel, BorderLayout.CENTER); // Team composition below dimensions

        mainPanel.add(centerPanel, BorderLayout.CENTER); // Add the combined panel to mainPanel

        // Bottom Panel for Start Game Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startGameButton = new JButton("Start Game");
        startGameButton.addActionListener(new StartGameListener());
        bottomPanel.add(startGameButton);

        // Add everything to the frame
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH); // Place button at the very bottom

        loadConfig();
        setVisible(true);
    }




    private JLabel loadImage(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(new File(path)));
            Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaledImage));
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }

    private JComboBox<Integer> createAgentDropdown(int maxValue) {
        Integer[] values = new Integer[maxValue + 1]; // Allow values from 0 to maxValue
        for (int i = 0; i <= maxValue; i++) values[i] = i;
        return new JComboBox<>(values);
    }


    private String generateAgentConfig(Map<String, Integer> agentCounts) {
        StringBuilder agentsConfig = new StringBuilder();
        int blueCounter = 1, redCounter = 1;

        for (Map.Entry<String, Integer> entry : agentCounts.entrySet()) {
            String teamPrefix = entry.getKey();
            int count = entry.getValue();

            for (int i = 0; i < count; i++) {
                String agentType = teamPrefix.contains("gatherer") ? "gatherer_agent"
                        : teamPrefix.contains("archer") ? "archer_agent"
                        : teamPrefix.contains("warrior") ? "warrior_agent"
                        : "priest_agent";

                String agentName = teamPrefix + (teamPrefix.endsWith("_b") ? blueCounter++ : redCounter++);
                agentsConfig.append("\t").append(agentName).append(" ").append(agentType).append(";\n");
            }
        }

        return agentsConfig.toString();
    }

    private class StartGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveConfig();

            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());

            Map<String, Integer> agentCounts = new LinkedHashMap<>();
            agentCounts.put("gatherer_b", (Integer) gathererBlue.getSelectedItem());
            agentCounts.put("archer_b", (Integer) archerBlue.getSelectedItem());
            agentCounts.put("warrior_b", (Integer) warriorBlue.getSelectedItem());
            agentCounts.put("priest_b", (Integer) priestBlue.getSelectedItem());

            agentCounts.put("gatherer_r", (Integer) gathererRed.getSelectedItem());
            agentCounts.put("archer_r", (Integer) archerRed.getSelectedItem());
            agentCounts.put("warrior_r", (Integer) warriorRed.getSelectedItem());
            agentCounts.put("priest_r", (Integer) priestRed.getSelectedItem());

            String agentsConfig = generateAgentConfig(agentCounts);
            updateMas2jFile(width, height, agentsConfig);
            ConfigWindow.this.dispose();

            try {
                launchGame();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    private void addAgentSelectionListener(JComboBox<Integer>[] teamDropdowns, boolean isBlueTeam) {
        for (JComboBox<Integer> dropdown : teamDropdowns) {
            dropdown.addActionListener(e -> {
                int totalAgents = 0;
                int gathererCount = 0;

                for (int i = 0; i < teamDropdowns.length; i++) {
                    int value = (int) teamDropdowns[i].getSelectedItem();
                    totalAgents += value;

                    if (i == 0) { // First dropdown is for Gatherers
                        gathererCount = value;
                    }
                }

                if (totalAgents > 4) {
                    JOptionPane.showMessageDialog(null, "Max 4 agents per team!", "Error", JOptionPane.ERROR_MESSAGE);
                    dropdown.setSelectedItem(0);
                }
            });
        }
    }

    private void updateMas2jFile(int width, int height, String agentsConfig) {
        try {
            File mas2jFile = new File(MAS2J_FILE_PATH);
            if (!mas2jFile.exists()) {
                JOptionPane.showMessageDialog(null, "MAS2J file not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            StringBuilder newContent = new StringBuilder();
            newContent.append("MAS princess {\n")
                    .append("\tinfrastructure: Centralised\n")
                    .append("\tenvironment: env.BlackForestEnvironment(").append(width).append(", ").append(height).append(")\n")
                    .append("\tagents:\n").append(agentsConfig)
                    .append("\taslSourcePath:\n\t\"src/main/asl\";\n}");

            BufferedWriter writer = new BufferedWriter(new FileWriter(mas2jFile));
            writer.write(newContent.toString());
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void launchGame() throws IOException {

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", "runMas_princessMas");
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(projectRoot);

            gameProcess = processBuilder.start();

            // Read output asynchronously to prevent blocking
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(gameProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();

        }




    private void saveConfig() {
        Properties configProps = new Properties();
        configProps.setProperty("width", widthField.getText());
        configProps.setProperty("height", heightField.getText());

        // Save the selections of each dropdown
        configProps.setProperty("gatherer_blue", gathererBlue.getSelectedItem().toString());
        configProps.setProperty("archer_blue", archerBlue.getSelectedItem().toString());
        configProps.setProperty("warrior_blue", warriorBlue.getSelectedItem().toString());
        configProps.setProperty("priest_blue", priestBlue.getSelectedItem().toString());

        configProps.setProperty("gatherer_red", gathererRed.getSelectedItem().toString());
        configProps.setProperty("archer_red", archerRed.getSelectedItem().toString());
        configProps.setProperty("warrior_red", warriorRed.getSelectedItem().toString());
        configProps.setProperty("priest_red", priestRed.getSelectedItem().toString());

        try (FileOutputStream out = new FileOutputStream(configFile)) {
            configProps.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        if (configFile.exists()) {
            Properties configProps = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)) {
                configProps.load(in);

                widthField.setText(configProps.getProperty("width", "40"));
                heightField.setText(configProps.getProperty("height", "15"));

                // Set the dropdown values
                gathererBlue.setSelectedItem(Integer.parseInt(configProps.getProperty("gatherer_blue", "0")));
                archerBlue.setSelectedItem(Integer.parseInt(configProps.getProperty("archer_blue", "0")));
                warriorBlue.setSelectedItem(Integer.parseInt(configProps.getProperty("warrior_blue", "0")));
                priestBlue.setSelectedItem(Integer.parseInt(configProps.getProperty("priest_blue", "0")));

                gathererRed.setSelectedItem(Integer.parseInt(configProps.getProperty("gatherer_red", "0")));
                archerRed.setSelectedItem(Integer.parseInt(configProps.getProperty("archer_red", "0")));
                warriorRed.setSelectedItem(Integer.parseInt(configProps.getProperty("warrior_red", "0")));
                priestRed.setSelectedItem(Integer.parseInt(configProps.getProperty("priest_red", "0")));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void showGameResult(String winningTeam) {
        JFrame resultFrame = new JFrame("Game Over");
        resultFrame.setSize(400, 200);
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setLayout(new BorderLayout());

        Color lightBlue = new Color(173, 216, 230);
        Color lightRed = new Color(255, 182, 193);
        Color backgroundColor = winningTeam.equals("Red Team") ? lightRed : lightBlue;
        Color textColor = winningTeam.equals("Red Team") ? Color.RED : Color.BLUE;

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(backgroundColor);
        contentPanel.setLayout(new BorderLayout());

        JLabel resultLabel = new JLabel(winningTeam + " wins!", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setForeground(textColor);
        contentPanel.add(resultLabel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.setLayout(new FlowLayout());

        // Restart with new configuration
        JButton restartNewConfigButton = new JButton("Restart with New Config");
        restartNewConfigButton.addActionListener(e -> {
            resultFrame.dispose();
            terminateGameProcess();
            restartJavaProcess();
            SwingUtilities.invokeLater(() -> new ConfigWindow());
        });

        // Exit Game
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            terminateGameProcess();
            System.exit(0);
        });

        // Add buttons to panel
        buttonPanel.add(restartNewConfigButton);
        buttonPanel.add(exitButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultFrame.add(contentPanel);
        resultFrame.setLocationRelativeTo(null);
        resultFrame.setVisible(true);
    }

    private static void terminateGameProcess() {
        if (gameProcess != null) {
            gameProcess.toHandle().destroyForcibly();
            try {
                gameProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void restartJavaProcess() {
        try {
            String javaBin = System.getProperty("java.home") + "/bin/java";
            File currentJar = new File(ConfigWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            if (currentJar.getName().endsWith(".jar")) {
                // Restart JAR
                new ProcessBuilder(javaBin, "-jar", currentJar.getPath()).start();
            } else {
                // Restart class-based execution
                new ProcessBuilder(javaBin, "-cp", System.getProperty("java.class.path"), ConfigWindow.class.getName()).start();
            }

            System.exit(0); // Kill current process
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new ConfigWindow();
    }
}
