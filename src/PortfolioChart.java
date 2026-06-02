import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PortfolioChart extends JPanel {
    private final List<Double> history = new ArrayList<>();
    private final Color COLOR_BG = new Color(28, 34, 46);
    private final Color COLOR_LINE = new Color(33, 150, 243);
    private final Color COLOR_GRID = new Color(42, 52, 71);

    public PortfolioChart() {
        setBackground(COLOR_BG);
    }

    public void addValue(double value) {
        history.add(value);
        if (history.size() > 40) {
            history.remove(0);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 15;

        g2.setColor(COLOR_GRID);
        g2.drawRect(padding, padding, width - (2 * padding), height - (2 * padding));

        if (history.size() < 2) {
            g2.setColor(Color.GRAY);
            g2.drawString("Gathering Real-Time NAV Data...", width / 2 - 80, height / 2);
            return;
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double val : history) {
            if (val > max) max = val;
            if (val < min) min = val;
        }

        if (max == min) { max += 100; min -= 100; }
        double range = max - min;

        double xScale = (double) (width - 2 * padding) / (history.size() - 1);
        double yScale = (double) (height - 2 * padding) / range;

        g2.setColor(COLOR_LINE);
        g2.setStroke(new BasicStroke(2.5f));

        for (int i = 0; i < history.size() - 1; i++) {
            int x1 = padding + (int) (i * xScale);
            int y1 = height - padding - (int) ((history.get(i) - min) * yScale);
            int x2 = padding + (int) ((i + 1) * xScale);
            int y2 = height - padding - (int) ((history.get(i + 1) - min) * yScale);

            g2.drawLine(x1, y1, x2, y2);
        }
    }
}