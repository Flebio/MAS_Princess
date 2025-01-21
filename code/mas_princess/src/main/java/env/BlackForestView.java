//package env.BlackForest;
//
//import env.*;
//import env.utils.*;
//
//import javax.swing.*;
//import java.awt.*;
//import java.lang.reflect.InvocationTargetException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Random;
//
//public class BlackForestView extends JFrame implements MapView {
//
//    private static final Random RAND = new Random();
//
//    private static Color randomColor() {
//        return new Color(RAND.nextInt(256), RAND.nextInt(256), RAND.nextInt(256));
//    }
//
//    private static Color negateColor(Color color) {
//        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
//    }
//
//    private static Color smoothColor(Color color) {
//        return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2);
//    }
//
//    private static Color mix(Color c1, Color c2) {
//        return new Color(
//                (c1.getRed() * c1.getAlpha() + c2.getRed() * c2.getAlpha()) / 255 % 256,
//                (c1.getGreen() * c1.getAlpha() + c2.getGreen() * c2.getAlpha()) / 255 % 256,
//                (c1.getBlue() * c1.getAlpha() + c2.getBlue() * c2.getAlpha()) / 255 % 256,
//                Math.min(c1.getAlpha(), c2.getAlpha())
//        );
//    }
//
//    private final MapModel model;
//    private final Map<Vector2D, JButton> buttonsGrid = new HashMap<>();
//    private final Map<String, Color> agentColors = new HashMap<>();
//
//    public BlackForestView(MapModel model) {
//        this.model = Objects.requireNonNull(model);
//
//        JPanel contentPane = new JPanel(new BorderLayout());
//        JPanel grid = new JPanel(new GridLayout(model.getHeight(), model.getWidth()));
//        for (int y = 0; y < model.getHeight(); y++) {
//            for (int x = 0; x < model.getWidth(); x++) {
//                JButton b = new JButton("   ");
//                grid.add(b);
//                buttonsGrid.put(Vector2D.of(x, y), b);
//            }
//        }
//        contentPane.add(grid, BorderLayout.CENTER);
//        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 60, (int) model.getFPS());
//        contentPane.add(slider, BorderLayout.SOUTH);
//        slider.addChangeListener(e -> model.setFPS(slider.getValue()));
//        setContentPane(contentPane);
//        pack();
//    }
//
//    private Color getColorForAgent(String agent) {
//        if (!agentColors.containsKey(agent)) {
//            agentColors.put(agent, randomColor());
//        }
//        return agentColors.get(agent);
//    }
//
//    @Override
//    public MapModel getModel() {
//        return model;
//    }
//
////    private void updateView() {
////        buttonsGrid.values().forEach(b -> {
////            b.setText(" ");
////            b.setBackground(Color.WHITE);
////            b.setForeground(UIManager.getColor("Button.foreground"));
////            b.setEnabled(true);
////        });
////        model.getAllAgents().forEach(a -> {
////            Vector2D pos = model.getAgentPosition(a);
////            Orientation dir = model.getAgentDirection(a);
////            JButton b = buttonsGrid.get(pos);
////            b.setText(dir.getSymbol());
////            Color c = getColorForAgent(a);
////            b.setBackground(c);
////            b.setForeground(negateColor(c));
////            b.setEnabled(false);
////            Color s = smoothColor(c);
////            model.getAgentSurroundingPositions(a).values().forEach(p -> {
////                JButton b1 = buttonsGrid.get(p);
////                if (b1 != null) b1.setBackground(mix(b1.getBackground(), s));
////            });
////        });
////        repaint();
//    }
//
////    @Override
////    public void notifyModelChanged() {
////        try {
////            SwingUtilities.invokeAndWait(this::updateView);
////        } catch (InterruptedException | InvocationTargetException e) {
////            e.printStackTrace();
////        }
////    }
//}
