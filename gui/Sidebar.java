package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    private static final Color BG          = new Color(24, 24, 28);
    private static final Color FG          = new Color(200, 200, 210);
    private static final Color FG_ACTIVE   = Color.WHITE;
    private static final Color ACCENT      = new Color(74, 158, 255);
    private static final Color HOVER_BG    = new Color(255, 255, 255, 18);
    private static final Color ACTIVE_BG   = new Color(74, 158, 255, 40);
    private static final Color SEPARATOR   = new Color(255, 255, 255, 20);

    private final List<MenuItem> items = new ArrayList<>();
    private MenuItem activeItem;
    private Consumer<String> navigationListener;
    private Runnable exitAction;
    private Runnable languageAction;
    private JLabel titleText;
    private JButton languageButton;
    private MenuItem exitItem;

    public Sidebar() {
        setPreferredSize(new Dimension(240, 0));
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 0, 20, 0));
        applyComponentOrientation(LanguageManager.orientation());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel titleIcon = new JLabel("\uD83D\uDCDA");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        titleIcon.setForeground(ACCENT);

        titleText = new JLabel(LanguageManager.text("app.name"));
        titleText.setFont(new Font("Tahoma", Font.BOLD, 20));
        titleText.setForeground(ACCENT);

        titlePanel.add(titleIcon);
        titlePanel.add(titleText);
        add(titlePanel);

        add(Box.createRigidArea(new Dimension(0, 18)));
        add(createSeparator());
        add(Box.createRigidArea(new Dimension(0, 14)));

        addMenuItem("\uD83C\uDFE0", "home", "nav.home");
        addMenuItem("\uD83D\uDCD6", "books", "nav.books");
        addMenuItem("\uD83D\uDC65", "borrowers", "nav.borrowers");
        addMenuItem("\uD83D\uDD04", "borrowing", "nav.borrowing");
        addMenuItem("\u23F3",       "waitlist", "nav.waitlist");
        addMenuItem("\uD83D\uDCCA", "reports", "nav.reports");

        add(Box.createVerticalGlue());

        add(createLanguageButton());
        add(Box.createRigidArea(new Dimension(0, 8)));
        add(createSeparator());
        add(Box.createRigidArea(new Dimension(0, 8)));

        exitItem = new MenuItem("\u2192", "exit", "nav.exit");
        exitItem.setExitAction(() -> {
            if (exitAction != null) {
                exitAction.run();
            } else {
                int choice = JOptionPane.showConfirmDialog(
                        Sidebar.this,
                        LanguageManager.text("dialog.exit.message"),
                        LanguageManager.text("dialog.exit.title"),
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) System.exit(0);
            }
        });
        add(exitItem);
        add(Box.createRigidArea(new Dimension(0, 4)));

        if (!items.isEmpty()) setActive(items.get(0));
        updateLanguageTexts();
    }

    public void setNavigationListener(Consumer<String> listener) {
        this.navigationListener = listener;
    }

    public void setExitAction(Runnable action) {
        this.exitAction = action;
    }

    public void setLanguageAction(Runnable action) {
        this.languageAction = action;
    }

    public void updateLanguageTexts() {
        applyComponentOrientation(LanguageManager.orientation());
        titleText.setText(LanguageManager.text("app.name"));

        for (MenuItem item : items) {
            item.updateLanguageText();
        }

        if (exitItem != null) {
            exitItem.updateLanguageText();
        }

        if (languageButton != null) {
            languageButton.setText(LanguageManager.text("language.toggle"));
            languageButton.setToolTipText(LanguageManager.text("language.tooltip"));
        }

        revalidate();
        repaint();
    }

    private JComponent createSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(SEPARATOR);
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setPreferredSize(new Dimension(200, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sep;
    }

    private JButton createLanguageButton() {
        languageButton = new JButton(LanguageManager.text("language.toggle"));
        languageButton.setMaximumSize(new Dimension(200, 36));
        languageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        languageButton.setFocusPainted(false);
        languageButton.setBorderPainted(false);
        languageButton.setBackground(new Color(34, 34, 42));
        languageButton.setForeground(FG_ACTIVE);
        languageButton.setFont(new Font("Tahoma", Font.BOLD, 12));
        languageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        languageButton.setToolTipText(LanguageManager.text("language.tooltip"));
        languageButton.addActionListener(e -> {
            SoundManager.play("click");
            if (languageAction != null) {
                languageAction.run();
            }
        });
        return languageButton;
    }

    private void addMenuItem(String icon, String route, String textKey) {
        MenuItem item = new MenuItem(icon, route, textKey);
        items.add(item);
        add(item);
        add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void setActive(MenuItem item) {
        if (activeItem == item) return;
        if (activeItem != null) activeItem.setActive(false);
        activeItem = item;
        item.setActive(true);
        if (navigationListener != null) navigationListener.accept(item.getRoute());
    }

    private class MenuItem extends JPanel {
        private final JLabel iconLabel;
        private final JLabel label;
        private final String route;
        private final String textKey;
        private boolean active = false;
        private boolean hover  = false;
        private float bgAlpha  = 0f;
        private float indicatorWidth = 0f;
        private Timer animator;
        private Runnable exitAction;

        MenuItem(String icon, String route, String textKey) {
            this.route = route;
            this.textKey = textKey;
            setLayout(new BorderLayout(10, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(240, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 20, 0, 20));
            applyComponentOrientation(LanguageManager.orientation());

            label = new JLabel(LanguageManager.text(textKey));
            label.setFont(new Font("Tahoma", Font.PLAIN, 14));
            label.setForeground(FG);
            label.setHorizontalAlignment(LanguageManager.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
            add(label, BorderLayout.CENTER);

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            iconLabel.setForeground(FG);
            add(iconLabel, BorderLayout.WEST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  animate(); }
                @Override public void mouseExited (MouseEvent e) { hover = false; animate(); }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (exitAction != null) {
                        SoundManager.play("exit");
                        exitAction.run();
                    } else {
                        SoundManager.play("navigate");
                        Sidebar.this.setActive(MenuItem.this);
                    }
                }
            });
        }

        String getRoute() { return route; }
        void setExitAction(Runnable r) { this.exitAction = r; }

        void updateLanguageText() {
            applyComponentOrientation(LanguageManager.orientation());
            label.setText(LanguageManager.text(textKey));
            label.setHorizontalAlignment(LanguageManager.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
            repaint();
        }

        void setActive(boolean a) {
            this.active = a;
            Color c = a ? FG_ACTIVE : FG;
            label.setForeground(c);
            iconLabel.setForeground(c);
            label.setFont(new Font("Tahoma", a ? Font.BOLD : Font.PLAIN, 14));
            animate();
        }

        private void animate() {
            if (animator != null && animator.isRunning()) animator.stop();
            animator = new Timer(15, null);
            animator.addActionListener(e -> {
                float targetAlpha = active ? 1f : (hover ? 0.6f : 0f);
                float targetInd   = active ? 4f : 0f;

                bgAlpha        += (targetAlpha - bgAlpha)        * 0.25f;
                indicatorWidth += (targetInd   - indicatorWidth) * 0.25f;

                if (Math.abs(targetAlpha - bgAlpha) < 0.01f
                        && Math.abs(targetInd   - indicatorWidth) < 0.1f) {
                    bgAlpha = targetAlpha;
                    indicatorWidth = targetInd;
                    animator.stop();
                }
                repaint();
            });
            animator.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            if (bgAlpha > 0.01f) {
                Color base = active ? ACTIVE_BG : HOVER_BG;
                int a = (int) (base.getAlpha() * bgAlpha);
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
                g2.fillRoundRect(8, 4, w - 16, h - 8, 10, 10);
            }

            if (indicatorWidth > 0.1f) {
                g2.setColor(ACCENT);
                if (LanguageManager.isArabic()) {
                    g2.fillRoundRect(w - (int) indicatorWidth, 8, (int) indicatorWidth, h - 16, 4, 4);
                } else {
                    g2.fillRoundRect(0, 8, (int) indicatorWidth, h - 16, 4, 4);
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}