package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

public class MainFrame extends JFrame {

    private final LibraryManager library = new LibraryManager();
    private FadeLayerUI fadeLayer;
    private JLayer<JPanel> rootLayer;
    private ContentPanel contentPanel;
    private Sidebar sidebar;
    private JLabel welcome;
    private String currentRoute = "home";

    public MainFrame() {
        setTitle(LanguageManager.text("app.title"));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        applyComponentOrientation(LanguageManager.orientation());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(18, 18, 22));
        root.applyComponentOrientation(LanguageManager.orientation());

        sidebar = new Sidebar();
        sidebar.setExitAction(this::confirmExit);
        sidebar.setLanguageAction(this::toggleLanguage);
        sidebar.setNavigationListener(this::navigateTo);

        contentPanel = new ContentPanel();
        contentPanel.setBackground(new Color(18, 18, 22));
        contentPanel.setContent(buildWelcome());

        root.add(sidebar, BorderLayout.EAST);
        root.add(contentPanel, BorderLayout.CENTER);

        fadeLayer = new FadeLayerUI();
        rootLayer = new JLayer<>(root, fadeLayer);
        setContentPane(rootLayer);
    }

    private void confirmExit() {
        SoundManager.play("warning");
        int r = JOptionPane.showConfirmDialog(
                this,
                LanguageManager.text("dialog.exit.message"),
                LanguageManager.text("dialog.confirm"),
                JOptionPane.YES_NO_OPTION
        );

        if (r == JOptionPane.YES_OPTION) {
            SoundManager.play("exit");
            Timer t = new Timer(450, e -> System.exit(0));
            t.setRepeats(false);
            t.start();
        } else {
            SoundManager.play("back");
        }
    }

    private void toggleLanguage() {
        LanguageManager.toggleLanguage();
        setTitle(LanguageManager.text("app.title"));
        applyComponentOrientation(LanguageManager.orientation());
        if (rootLayer != null) {
            rootLayer.applyComponentOrientation(LanguageManager.orientation());
        }
        if (sidebar != null) {
            sidebar.updateLanguageTexts();
        }
        navigateTo(currentRoute);
    }

    private JPanel buildWelcome() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(18, 18, 22));
        p.applyComponentOrientation(LanguageManager.orientation());
        welcome = new JLabel("");
        welcome.setForeground(new Color(230, 230, 235));
        welcome.setFont(new Font("Tahoma", Font.BOLD, 26));
        p.add(welcome);
        startTyping();
        return p;
    }

    private void navigateTo(String route) {
        currentRoute = route;
        SoundManager.play("navigate");
        JComponent next;

        try {
            switch (route) {
                case "home":
                    next = buildWelcome();
                    break;

                case "books":
                    next = new BooksPanel(library);
                    break;

                case "borrowers":
                    next = new BorrowersPanel(library);
                    break;

                case "borrowing":
                    next = new BorrowingPanel(library);
                    break;

                case "waitlist":
                    next = new WaitListPanel(library);
                    break;

                case "reports":
                    next = new ReportsPanel(library);
                    break;

                default:
                    next = buildPlaceholder(route);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            next = buildPlaceholder(LanguageManager.text("placeholder.error") + ": " + ex.getMessage());
        }

        next.applyComponentOrientation(LanguageManager.orientation());
        contentPanel.setContent(next);
    }

    private JPanel buildPlaceholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(18, 18, 22));
        p.applyComponentOrientation(LanguageManager.orientation());
        JLabel l = new JLabel(LanguageManager.text("placeholder.page") + ": " + name + " - " + LanguageManager.text("placeholder.building"));
        l.setForeground(new Color(160, 160, 170));
        l.setFont(new Font("Tahoma", Font.PLAIN, 18));
        p.add(l);
        return p;
    }

    public void fadeIn() {
        fadeLayer.start();
    }

    private void startTyping() {
        final String fullText = LanguageManager.text("welcome");
        final int[] i = {0};
        Timer t = new Timer(55, null);
        t.addActionListener(e -> {
            if (i[0] >= fullText.length()) {
                t.stop();
                startBlinkingCursor(fullText);
                return;
            }
            i[0]++;
            welcome.setText(fullText.substring(0, i[0]) + "|");
        });
        Timer delay = new Timer(300, e -> t.start());
        delay.setRepeats(false);
        delay.start();
    }

    private void startBlinkingCursor(String fullText) {
        final int[] count = {0};
        final boolean[] on = {true};
        Timer blink = new Timer(450, null);
        blink.addActionListener(e -> {
            on[0] = !on[0];
            welcome.setText(fullText + (on[0] ? "|" : " "));
            count[0]++;
            if (count[0] >= 6) {
                blink.stop();
                welcome.setText(fullText);
            }
        });
        blink.start();
    }

    private class FadeLayerUI extends LayerUI<JPanel> {
        private float alpha = 1f;
        private Timer timer;
        private long startTime;
        private final int duration = 650;

        void start() {
            alpha = 1f;
            startTime = System.currentTimeMillis();
            if (timer != null) timer.stop();
            timer = new Timer(16, e -> {
                long elapsed = System.currentTimeMillis() - startTime;
                alpha = Math.max(0f, 1f - elapsed / (float) duration);
                if (rootLayer != null) rootLayer.repaint();
                if (elapsed >= duration) {
                    alpha = 0f;
                    timer.stop();
                }
            });
            timer.start();
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (alpha > 0f) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, Math.round(alpha * 255)));
                g2.fillRect(0, 0, c.getWidth(), c.getHeight());
                g2.dispose();
            }
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SoundManager.preload();
        UIManager.put("Component.focusColor", new Color(74, 158, 255));
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setVisible(true);
            f.fadeIn();
        });
    }
}