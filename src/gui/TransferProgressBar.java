package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 * A custom Swing progress bar with a simple percentage and fill display.
 */
public final class TransferProgressBar extends JProgressBar {
    private static final Color PROGRESS_COLOR = new Color(40, 114, 255);

    public TransferProgressBar() {
        setMinimum(0);
        setMaximum(100);
        setValue(0);
        setPreferredSize(new Dimension(400, 22));
        setOpaque(true);
        setBorderPainted(false);
        setUI(new CustomProgressBarUI());
    }

    public void setIndeterminate(boolean indeterminate) {
        super.setIndeterminate(indeterminate);
        repaint();
    }

    private static final class CustomProgressBarUI extends BasicProgressBarUI {
        protected void paint(Graphics graphics, javax.swing.JComponent component, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            if (((JProgressBar) component).isIndeterminate()) {
                g.setColor(PROGRESS_COLOR);
                g.fillRect(0, 0, width, height);
                g.setColor(Color.BLACK);
                g.drawString("Transferring...", 2, height - 4);
            } else {
                int progress = ((JProgressBar) component).getValue();
                if (progress > 0) {
                    int progressWidth = width * progress / ((JProgressBar) component).getMaximum();
                    g.setColor(PROGRESS_COLOR);
                    g.fillRect(0, 0, progressWidth, height);
                }
                g.setColor(Color.BLACK);
                g.drawString(progress + "%", 1, height - 4);
            }
            g.dispose();
        }
    }
}
