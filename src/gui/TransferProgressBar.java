package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicProgressBarUI;

/**
 * A custom Swing progress bar with a flat UI, custom colors, and dynamic status text.
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
        setStringPainted(true); // Ensures the string is tracked internally
        setUI(new CustomProgressBarUI());
    }

    /**
     * Safely updates the progress UI from background network threads.
     * Prevents EDT (Event Dispatch Thread) violations.
     */
    public void updateStatus(int percentage, String text) {
        SwingUtilities.invokeLater(() -> {
            setValue(percentage);
            setString(text); // Stores the custom text (e.g., "Sending File.txt")
        });
    }

    public void setIndeterminate(boolean indeterminate) {
        super.setIndeterminate(indeterminate);
        repaint();
    }

    private static final class CustomProgressBarUI extends BasicProgressBarUI {
        @Override
        public void paint(Graphics graphics, javax.swing.JComponent component) {
            // Extract width and height directly from the component
            int width = component.getWidth();
            int height = component.getHeight();

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw Background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            
            JProgressBar bar = (JProgressBar) component;
            String dynamicText = bar.getString() != null ? bar.getString() : "";

            if (bar.isIndeterminate()) {
                g.setColor(PROGRESS_COLOR);
                g.fillRect(0, 0, width, height);
                g.setColor(Color.WHITE);
                g.drawString(dynamicText.isEmpty() ? "Transferring..." : dynamicText, 5, height - 6);
            } else {
                int progress = bar.getValue();
                if (progress > 0) {
                    int progressWidth = (int) ((double) width * progress / bar.getMaximum());
                    g.setColor(PROGRESS_COLOR);
                    g.fillRect(0, 0, progressWidth, height);
                }
                g.setColor(Color.BLACK);
                
                // Combine the Dashboard status text with the percentage
                String statusText = dynamicText.isEmpty() ? progress + "%" : dynamicText + " (" + progress + "%)";
                g.drawString(statusText, 5, height - 6);
            }
            g.dispose();
        }
    }
}