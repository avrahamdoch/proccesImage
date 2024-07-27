import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Processing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            ImagePanel imagePanel = new ImagePanel();
            frame.add(imagePanel, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel();
            JButton openButton = new JButton("פתח תמונה");
            controlPanel.add(openButton);

            JComboBox<String> manipulationBox = new JComboBox<>(new String[]{
                    "Black-White", "Grayscale", "Posterize", "Tint",
                    "Color Shift Right", "Color Shift Left", "Mirror",
                    "Pixelate", "Show Borders", "Negative"
            });
            controlPanel.add(manipulationBox);

            JButton applyButton = new JButton("Apply");
            controlPanel.add(applyButton);

            frame.add(controlPanel, BorderLayout.SOUTH);

            openButton.addActionListener(e -> imagePanel.loadImage());
            applyButton.addActionListener(e -> imagePanel.applyManipulation((String) manipulationBox.getSelectedItem()));

            frame.setVisible(true);
        });
    }
}

class ImagePanel extends JPanel {
    private BufferedImage originalImage;
    private BufferedImage manipulatedImage;
    private int dividerX = -1;

    public ImagePanel() {
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                dividerX = e.getX();
                repaint();
            }
        });
    }

    public void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(file);
                manipulatedImage = null;
                dividerX = -1;
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void applyManipulation(String manipulation) {
        if (originalImage == null) return;

        manipulatedImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                Color color = new Color(originalImage.getRGB(x, y));
                Color newColor;

                switch (manipulation) {
                    case "Black-White":
                        newColor = toBlackWhite(color);
                        break;
                    case "Grayscale":
                        newColor = toGrayscale(color);
                        break;
                    case "Posterize":
                        newColor = toPosterize(color);
                        break;
                    case "Tint":
                        newColor = toTint(color);
                        break;
                    case "Color Shift Right":
                        newColor = toColorShiftRight(color);
                        break;
                    case "Color Shift Left":
                        newColor = toColorShiftLeft(color);
                        break;
                    case "Mirror":
                        newColor = toMirror(color, x, y);
                        break;
                    case "Pixelate":
                        newColor = toPixelate(x, y);
                        break;
                    case "Show Borders":
                        newColor = toShowBorders(color, x, y);
                        break;
                    case "Negative":
                        newColor = toNegative(color);
                        break;
                    default:
                        newColor = color;
                }

                manipulatedImage.setRGB(x, y, newColor.getRGB());
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (originalImage != null) {
            int width = getWidth();
            int height = getHeight();
            int imageWidth = originalImage.getWidth();
            int imageHeight = originalImage.getHeight();

            g.drawImage(originalImage, 0, 0, width, height, null);

            if (manipulatedImage != null) {
                g.drawImage(manipulatedImage, 0, 0, dividerX, height, 0, 0, dividerX * imageWidth / width, imageHeight, null);
            }

            if (dividerX > 0) {
                g.setColor(Color.RED);
                g.drawLine(dividerX, 0, dividerX, height);
            }
        }
    }

    private Color toBlackWhite(Color color) {
        int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return avg > 127 ? Color.WHITE : Color.BLACK;
    }

    private Color toGrayscale(Color color) {
        int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return new Color(avg, avg, avg);
    }

    private Color toPosterize(Color color) {
        int r = (color.getRed() / 64) * 64;
        int g = (color.getGreen() / 64) * 64;
        int b = (color.getBlue() / 64) * 64;
        return new Color(r, g, b);
    }

    private Color toTint(Color color) {
        int r = (color.getRed() + 128) / 2;
        int g = color.getGreen();
        int b = color.getBlue();
        return new Color(r, g, b);
    }

    private Color toColorShiftRight(Color color) {
        return new Color(color.getGreen(), color.getBlue(), color.getRed());
    }

    private Color toColorShiftLeft(Color color) {
        return new Color(color.getBlue(), color.getRed(), color.getGreen());
    }

    private Color toMirror(Color color, int x, int y) {
        int width = originalImage.getWidth();
        return new Color(originalImage.getRGB(width - 1 - x, y));
    }

    private Color toPixelate(int x, int y) {
        int pixelSize = 10;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int nx = (x / pixelSize) * pixelSize;
        int ny = (y / pixelSize) * pixelSize;
        return new Color(originalImage.getRGB(nx, ny));
    }

    private Color toShowBorders(Color color, int x, int y) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        if (x > 0 && y > 0 && x < width - 1 && y < height - 1) {
            Color left = new Color(originalImage.getRGB(x - 1, y));
            Color right = new Color(originalImage.getRGB(x + 1, y));
            Color up = new Color(originalImage.getRGB(x, y - 1));
            Color down = new Color(originalImage.getRGB(x, y + 1));

            int diff = Math.abs(left.getRed() - right.getRed()) +
                    Math.abs(left.getGreen() - right.getGreen()) +
                    Math.abs(left.getBlue() - right.getBlue()) +
                    Math.abs(up.getRed() - down.getRed()) +
                    Math.abs(up.getGreen() - down.getGreen()) +
                    Math.abs(up.getBlue() - down.getBlue());

            return diff > 100 ? Color.BLACK : Color.WHITE;
        }
        return color;
    }

    private Color toNegative(Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
    }
}