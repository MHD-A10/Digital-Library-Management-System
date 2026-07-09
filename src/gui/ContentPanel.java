package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ContentPanel extends JPanel {
    private JComponent current;
    private BufferedImage oldSnapshot;
    private BufferedImage newSnapshot;
    private float progress = 1f;
    private Timer animator;
    private final int duration = 350; 
    private long startTime;

    public ContentPanel() {
        setLayout(null);
        setOpaque(false);
    }

    public void setContent(JComponent next) {
        if (current != null) {
            oldSnapshot = snapshot(current);
            remove(current);
        }
        current = next;
        current.setBounds(0, 0, Math.max(getWidth(), 1), Math.max(getHeight(), 1));
        add(current);
        revalidate();
        newSnapshot = snapshot(current);

        if (oldSnapshot != null) {
            startSlide();
        } else {
            progress = 1f;
            repaint();
        }
    }


    private BufferedImage snapshot(JComponent c) {
        if (c.getWidth() <= 0 || c.getHeight() <= 0) return null;
        BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    private void startSlide() {
        if (animator != null && animator.isRunning()) animator.stop();
        progress = 0f;
        startTime = System.currentTimeMillis();
        current.setVisible(false);
        animator = new Timer(15, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            progress = Math.min(1f, elapsed / (float) duration);
            
            float eased = 1f - (float) Math.pow(1f - progress, 3);
            this.progress = eased;
            repaint();
            if (progress >= 1f) {
                ((Timer) e.getSource()).stop();
                oldSnapshot = null;
                current.setVisible(true);
                current.setBounds(0, 0, getWidth(), getHeight());
                repaint();
            }
        });
        animator.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (oldSnapshot != null && newSnapshot != null && progress < 1f) {
            int w = getWidth();
            int h = getHeight();
            int offset = (int) (w * progress);
            Graphics2D g2 = (Graphics2D) g.create();
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - progress));
            g2.drawImage(oldSnapshot, -offset / 2, 0, null);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, progress));
            g2.drawImage(newSnapshot, w - offset, 0, null);
            g2.dispose();
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (current != null) {
            current.setBounds(0, 0, getWidth(), getHeight());
        }
    }
}
