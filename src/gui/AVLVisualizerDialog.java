package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

public class AVLVisualizerDialog extends JDialog {

    
    private static final Color BG       = new Color(18, 18, 22);
    private static final Color SURFACE  = new Color(28, 28, 33);
    private static final Color SURFACE2 = new Color(34, 34, 40);
    private static final Color BORDER_C = new Color(45, 45, 52);
    private static final Color FG       = new Color(230, 230, 235);
    private static final Color MUTED    = new Color(160, 160, 170);
    private static final Color ACCENT   = new Color(74, 158, 255);
    private static final Color SUCCESS  = new Color(70, 180, 110);
    private static final Color WARN     = new Color(245, 181, 10);
    private static final Color DANGER   = new Color(220, 70, 75);

    private static final String FONT_AR  = "Tahoma";
    private static final String FONT_SYM = "Segoe UI Symbol";

    private final LibraryManager library;

    private TreePanel treePanel;
    private JLabel titleLabel;
    private StatBadge countLbl, avlHLbl, bstHLbl, diffLbl;
    private ToggleButton avlBtn, bstBtn;
    private boolean showingAVL = true;

    private Node avlRoot, bstRoot;
    private int avlHeight = 0, bstHeight = 0, totalBooks = 0;

    private static String tr(String key) {
        return LanguageManager.text(key);
    }

    public AVLVisualizerDialog(JFrame owner, LibraryManager library) {
        super(owner, tr("avl.dialogTitle"), true);
        this.library = library;

        setSize(1240, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        applyComponentOrientation(LanguageManager.orientation());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadData();
        switchTo(true, false);
        safePlay("open");
    }

    
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("\u2698");
        icon.setFont(new Font(FONT_SYM, Font.PLAIN, 22));
        icon.setForeground(ACCENT);
        titleLabel = new JLabel(tr("avl.title"));
        titleLabel.setFont(new Font(FONT_AR, Font.BOLD, 18));
        titleLabel.setForeground(FG);
        left.add(icon); left.add(titleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        avlBtn = new ToggleButton(tr("avl.avlButton"), SUCCESS);
        bstBtn = new ToggleButton(tr("avl.bstButton"), ACCENT);
        avlBtn.setSelectedState(true);
        avlBtn.addActionListener(e -> { safePlay("click"); switchTo(true, true); });
        bstBtn.addActionListener(e -> { safePlay("click"); switchTo(false, true); });

        GhostButton refresh = new GhostButton(tr("avl.refresh"), SURFACE2);
        refresh.addActionListener(e -> { safePlay("click"); loadData(); switchTo(showingAVL, true); showTopToast(tr("avl.updated"), SUCCESS); });

        right.add(avlBtn); right.add(bstBtn); right.add(refresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    
    private JComponent buildCenter() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(new EmptyBorder(12, 16, 12, 16));

        treePanel = new TreePanel();
        treePanel.setBackground(SURFACE);

        JScrollPane sp = new JScrollPane(treePanel);
        sp.setBorder(new RoundedBorder(14, BORDER_C));
        sp.getViewport().setBackground(SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);

        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(SURFACE);
        footer.setBorder(new EmptyBorder(8, 18, 8, 18));

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stats.setOpaque(false);

        countLbl = stat(tr("avl.booksCount"), "0", FG);
        avlHLbl  = stat(tr("avl.avlHeight"), "0", SUCCESS);
        bstHLbl  = stat(tr("avl.bstHeight"), "0", ACCENT);
        diffLbl  = stat(tr("avl.diff"), "0", WARN);

        stats.add(countLbl);
        stats.add(avlHLbl);
        stats.add(bstHLbl);
        stats.add(diffLbl);

        GhostButton close = new GhostButton(tr("avl.close"), DANGER);
        close.addActionListener(e -> { safePlay("click"); dispose(); });

        footer.add(stats, BorderLayout.CENTER);
        footer.add(close, BorderLayout.EAST);
        return footer;
    }

    private StatBadge stat(String name, String value, Color color) {
        return new StatBadge(name, value, color);
    }

    private class StatBadge extends JPanel {
        private final JLabel valueLabel;

        StatBadge(String name, String value, Color color) {
            super(new BorderLayout(10, 0));
            setOpaque(true);
            setBackground(SURFACE2);
            setBorder(BorderFactory.createCompoundBorder(
                    new CompactRoundedBorder(10, BORDER_C),
                    new EmptyBorder(4, 10, 4, 10)
            ));
            setPreferredSize(new Dimension(166, 38));
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(new Font(FONT_AR, Font.BOLD, 11));
            nameLabel.setForeground(MUTED);
            nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            nameLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font(FONT_AR, Font.BOLD, 13));
            valueLabel.setForeground(color);
            valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
            valueLabel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            add(valueLabel, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
        }

        void setValue(String value) { valueLabel.setText(value); }
    }

    private void updateStats() {
        countLbl.setValue(String.valueOf(totalBooks));
        avlHLbl.setValue(String.valueOf(avlHeight));
        bstHLbl.setValue(String.valueOf(bstHeight));
        diffLbl.setValue(String.valueOf(bstHeight - avlHeight));
    }

    private void switchTo(boolean avl, boolean animate) {
        showingAVL = avl;
        avlBtn.setSelectedState(avl);
        bstBtn.setSelectedState(!avl);
        if (avl) {
            titleLabel.setText(tr("avl.actualTitle"));
            treePanel.setData(avlRoot, SUCCESS);
        } else {
            titleLabel.setText(tr("avl.bstTitle"));
            treePanel.setData(bstRoot, ACCENT);
        }
        if (animate) treePanel.playEntranceAnimation();
        else treePanel.repaint();
    }

    
    private void loadData() {
        List<Book> books = new ArrayList<>();
        avlRoot = null;
        bstRoot = null;

        try {
            books = library.getBooksInInsertionOrder();

            Field f = LibraryManager.class.getDeclaredField("booksTree");
            f.setAccessible(true);
            BookAVLTree tree = (BookAVLTree) f.get(library);
            if (tree != null) {
                avlRoot = copyFromAVL(tree.getRoot());
            }
        } catch (Throwable ignored) {
            books = library.getBooks();
        }

        totalBooks = books.size();

        for (Book b : books) bstRoot = insertBST(bstRoot, b);

        if (avlRoot == null) {
            for (Book b : books) avlRoot = insertAVL(avlRoot, b);
        }

        refreshNodeHeights(avlRoot);
        refreshNodeHeights(bstRoot);
        avlHeight = heightOf(avlRoot);
        bstHeight = heightOf(bstRoot);
        updateStats();
    }

    
    private static class Node {
        Book book;
        Node left, right;
        int height = 1;
        double x, y;
        int depth = 0;
        Node(Book b) { this.book = b; }
    }

    private Node copyFromAVL(BookAVLNode avlNode) {
        if (avlNode == null) return null;
        Node node = new Node(avlNode.getBook());
        node.height = avlNode.getHeight();
        node.left = copyFromAVL(avlNode.getLeft());
        node.right = copyFromAVL(avlNode.getRight());
        return node;
    }
    private Node insertBST(Node n, Book b) {
        if (n == null) return new Node(b);
        int c = key(b).compareToIgnoreCase(key(n.book));
        if (c < 0) n.left = insertBST(n.left, b);
        else n.right = insertBST(n.right, b);
        return n;
    }
    private Node insertAVL(Node n, Book b) {
        if (n == null) return new Node(b);
        int c = key(b).compareToIgnoreCase(key(n.book));
        if (c < 0) n.left  = insertAVL(n.left,  b);
        else       n.right = insertAVL(n.right, b);
        n.height = 1 + Math.max(h(n.left), h(n.right));
        int bal = h(n.left) - h(n.right);
        if (bal >  1 && key(b).compareToIgnoreCase(key(n.left.book))  < 0) return rotR(n);
        if (bal < -1 && key(b).compareToIgnoreCase(key(n.right.book)) > 0) return rotL(n);
        if (bal >  1 && key(b).compareToIgnoreCase(key(n.left.book))  > 0) { n.left  = rotL(n.left);  return rotR(n); }
        if (bal < -1 && key(b).compareToIgnoreCase(key(n.right.book)) < 0) { n.right = rotR(n.right); return rotL(n); }
        return n;
    }
    private int h(Node n) { return n == null ? 0 : n.height; }
    private int refreshNodeHeights(Node n) {
        if (n == null) return 0;
        n.height = 1 + Math.max(refreshNodeHeights(n.left), refreshNodeHeights(n.right));
        return n.height;
    }
    private Node rotR(Node y) { Node x=y.left, t=x.right; x.right=y; y.left=t;
        y.height=1+Math.max(h(y.left),h(y.right)); x.height=1+Math.max(h(x.left),h(x.right)); return x; }
    private Node rotL(Node x) { Node y=x.right, t=y.left; y.left=x; x.right=t;
        x.height=1+Math.max(h(x.left),h(x.right)); y.height=1+Math.max(h(y.left),h(y.right)); return y; }
    private int heightOf(Node n) { return n==null?0:1+Math.max(heightOf(n.left),heightOf(n.right)); }
    private String key(Book b) {
        try { return String.valueOf(b.getISBN()); } catch (Throwable t) { return String.valueOf(b); }
    }


    private void showTopToast(String msg, Color color) {
        JRootPane rootPane = getRootPane();
        if (rootPane == null) {
            return;
        }

        JLayeredPane layer = rootPane.getLayeredPane();
        JLabel toast = new JLabel(msg, SwingConstants.CENTER);
        toast.setOpaque(true);
        toast.setBackground(color.darker());
        toast.setForeground(Color.WHITE);
        toast.setFont(new Font(FONT_AR, Font.BOLD, 13));
        toast.setBorder(new EmptyBorder(10, 22, 10, 22));

        Dimension size = toast.getPreferredSize();
        int x = (layer.getWidth() - size.width) / 2;
        toast.setBounds(x, -size.height, size.width, size.height);
        layer.add(toast, JLayeredPane.POPUP_LAYER);

        final int targetY = 18;
        final long start = System.currentTimeMillis();
        Timer timer = new Timer(15, null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            int phase = 0;
            long phaseStart = start;

            public void actionPerformed(java.awt.event.ActionEvent e) {
                long now = System.currentTimeMillis();
                if (phase == 0) {
                    float p = Math.min(1f, (now - phaseStart) / 240f);
                    int y = (int) (-size.height + (targetY + size.height) * p);
                    toast.setLocation(toast.getX(), y);
                    if (p >= 1f) {
                        phase = 1;
                        phaseStart = now;
                    }
                } else if (phase == 1) {
                    if (now - phaseStart >= 1300) {
                        phase = 2;
                        phaseStart = now;
                    }
                } else {
                    float p = Math.min(1f, (now - phaseStart) / 260f);
                    int y = (int) (targetY - (targetY + size.height) * p);
                    toast.setLocation(toast.getX(), y);
                    if (p >= 1f) {
                        timer.stop();
                        layer.remove(toast);
                        layer.repaint();
                    }
                }
            }
        });
        timer.start();
    }
    private void safePlay(String s) {
        try { SoundManager.play(s); } catch (Throwable ignored) {}
    }

    
    private class TreePanel extends JPanel {
        private Node root;
        private Color nodeColor = SUCCESS;
        private Timer anim, pulse;
        private float globalProgress = 1f;
        private float pulsePhase = 0f;
        private Node hoverNode = null;
        private final int NODE_W = 108, NODE_H = 44, V_GAP = 70, H_PAD = 28;
        private int idxCounter = 0;
        private int totalDepth = 1;

        
        private final java.util.List<Particle> particles = new ArrayList<>();
        private final Random rnd = new Random();

        TreePanel() {
            setOpaque(true);
            setPreferredSize(new Dimension(1040, 520));

            for (int i = 0; i < 36; i++) particles.add(new Particle());

            pulse = new Timer(33, e -> {
                pulsePhase += 0.07f;
                for (Particle p : particles) p.step(getWidth(), getHeight());
                repaint();
            });
            pulse.start();

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    Node hit = hitTest(root, e.getX(), e.getY());
                    if (hit != hoverNode) {
                        hoverNode = hit;
                        setCursor(hit!=null?
                                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR):Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) { hoverNode = null; repaint(); }
                @Override public void mousePressed(MouseEvent e) {
                    Node hit = hitTest(root, e.getX(), e.getY());
                    if (hit != null) safePlay("click");
                }
            });
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        @Override public String getToolTipText(MouseEvent e) {
            Node n = hitTest(root, e.getX(), e.getY());
            if (n == null) return null;
            try {
                return "<html><div style='padding:6px;font-family:Tahoma;'>" +
                        "<b>" + tr("avl.tooltip.title") + ":</b> " + n.book.getTitle() + "<br>" +
                        "<b>ISBN:</b> " + n.book.getISBN() + "<br>" +
                        "<b>" + tr("avl.tooltip.height") + ":</b> " + n.height + "</div></html>";
            } catch (Throwable t) { return key(n.book); }
        }

        private Node hitTest(Node n, int mx, int my) {
            if (n == null) return null;
            if (Math.abs(mx - n.x) <= NODE_W/2.0 && Math.abs(my - n.y) <= NODE_H/2.0) return n;
            Node l = hitTest(n.left, mx, my);
            if (l != null) return l;
            return hitTest(n.right, mx, my);
        }

        void setData(Node r, Color c) {
            this.root = r;
            this.nodeColor = c;
            layoutTree();
            revalidate();
            repaint();
        }

        void playEntranceAnimation() {
            if (anim != null && anim.isRunning()) anim.stop();
            globalProgress = 0f;
            final long start = System.currentTimeMillis();
            anim = new Timer(16, null);
            anim.addActionListener(e -> {
                float t = Math.min(1f, (System.currentTimeMillis() - start) / 760f);
                globalProgress = t;
                repaint();
                if (t >= 1f) { anim.stop(); }
            });
            anim.start();
        }

        private void layoutTree() {
            if (root == null) { setPreferredSize(new Dimension(1040, 430)); return; }
            int total = countNodes(root);
            int viewportWidth = getParent() == null ? 1040 : Math.max(1040, getParent().getWidth());
            int naturalWidth = total * (NODE_W + 18) + H_PAD * 2;
            int width = Math.max(viewportWidth, naturalWidth);
            totalDepth = Math.max(1, heightOf(root));
            int height = Math.max(430, (totalDepth + 1) * V_GAP + 68);
            idxCounter = 0;
            assignX(root, width, total);
            assignY(root, 0);
            setPreferredSize(new Dimension(width, height));
        }
        private int countNodes(Node n) { return n==null?0:1+countNodes(n.left)+countNodes(n.right); }
        private void assignX(Node n, int width, int total) {
            if (n == null) return;
            assignX(n.left, width, total);
            int pos = idxCounter++;
            n.x = H_PAD + (pos + 0.5) * ((width - H_PAD * 2) / (double) Math.max(1,total));
            assignX(n.right, width, total);
        }
        private void assignY(Node n, int depth) {
            if (n == null) return;
            n.depth = depth;
            n.y = 48 + depth * V_GAP;
            assignY(n.left, depth + 1);
            assignY(n.right, depth + 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            
            GradientPaint bg = new GradientPaint(0,0,SURFACE,0,getHeight(),
                    new Color(22,22,28));
            g2.setPaint(bg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            
            g2.setColor(new Color(255, 255, 255, 5));
            for (int x = 0; x < getWidth(); x += 30) g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 30) g2.drawLine(0, y, getWidth(), y);

            
            for (Particle p : particles) p.draw(g2, nodeColor);

            if (root == null) {
                g2.setColor(MUTED);
                g2.setFont(new Font(FONT_AR, Font.PLAIN, 16));
                String msg = tr("avl.empty");
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            drawEdges(g2, root);
            drawNodes(g2, root);
            g2.dispose();
        }

        
        private float depthProgress(int depth) {
            float perDepth = 1f / (totalDepth + 1);
            float start = depth * perDepth * 0.8f;
            float t = (globalProgress - start) / (1f - start);
            t = Math.max(0f, Math.min(1f, t));
            
            float s = 1.70158f;
            float u = t - 1f;
            return (float) (u * u * ((s + 1) * u + s) + 1f);
        }

        private void drawEdges(Graphics2D g2, Node n) {
            if (n == null) return;
            if (n.left != null)  drawEdge(g2, n, n.left);
            if (n.right != null) drawEdge(g2, n, n.right);
            drawEdges(g2, n.left);
            drawEdges(g2, n.right);
        }

        private void drawEdge(Graphics2D g2, Node parent, Node child) {
            float pChild = Math.max(0f, Math.min(1f, depthProgress(child.depth)));
            if (pChild <= 0.02f) return;

            boolean highlighted = hoverNode != null && containsNode(child, hoverNode);

            double x1 = parent.x, y1 = parent.y + NODE_H/2.0;
            double x2 = child.x,  y2 = child.y  - NODE_H/2.0;
            double cx1 = x1, cy1 = (y1 + y2) / 2.0;
            double cx2 = x2, cy2 = (y1 + y2) / 2.0;

            CubicCurve2D curve = new CubicCurve2D.Double(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
            Shape partial = partialCurve(curve, pChild);

            int softAlpha = hoverNode == null ? 38 : (highlighted ? 76 : 18);
            int lineAlpha = hoverNode == null ? 155 : (highlighted ? 235 : 55);
            float glowWidth = highlighted ? 9f : 5f;
            float lineWidth = highlighted ? 2.8f : 1.5f;

            g2.setStroke(new BasicStroke(glowWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(),
                    (int) (softAlpha * pChild)));
            g2.draw(partial);

            if (highlighted) {
                g2.setStroke(new BasicStroke(5.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(255, 255, 255, (int) (34 * pChild)));
                g2.draw(partial);
            }

            g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (highlighted) {
                g2.setColor(new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(),
                        (int) (lineAlpha * pChild)));
            } else {
                g2.setColor(new Color(175, 185, 205, (int) (lineAlpha * pChild)));
            }
            g2.draw(partial);

            if (highlighted && pChild > 0.98f) {
                double[] pts = {x1, y1, cx1, cy1, cx2, cy2, x2, y2};
                double travel = (Math.sin(pulsePhase * 1.6) + 1) / 2.0;
                double[] dot = bezier(pts, travel);
                double r = 3.2 + 1.0 * Math.abs(Math.sin(pulsePhase * 1.6));
                g2.setColor(new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), 80));
                g2.fill(new Ellipse2D.Double(dot[0] - r * 2, dot[1] - r * 2, r * 4, r * 4));
                g2.setColor(new Color(245, 250, 255, 230));
                g2.fill(new Ellipse2D.Double(dot[0] - r, dot[1] - r, r * 2, r * 2));
            }
        }

        private boolean containsNode(Node current, Node target) {
            if (current == null || target == null) return false;
            if (current == target) return true;
            return containsNode(current.left, target) || containsNode(current.right, target);
        }

        private Shape partialCurve(CubicCurve2D c, float t) {
            if (t >= 1f) return c;
            
            CubicCurve2D left = new CubicCurve2D.Double();
            CubicCurve2D right = new CubicCurve2D.Double();
            
            int steps = 20;
            Path2D path = new Path2D.Double();
            double[] pts = new double[8];
            pts[0]=c.getX1(); pts[1]=c.getY1();
            pts[2]=c.getCtrlX1(); pts[3]=c.getCtrlY1();
            pts[4]=c.getCtrlX2(); pts[5]=c.getCtrlY2();
            pts[6]=c.getX2(); pts[7]=c.getY2();
            path.moveTo(pts[0], pts[1]);
            for (int i = 1; i <= steps; i++) {
                double u = (i / (double) steps) * t;
                double[] p = bezier(pts, u);
                path.lineTo(p[0], p[1]);
            }
            return path;
        }

        private double[] bezier(double[] p, double t) {
            double u = 1 - t;
            double x = u*u*u*p[0] + 3*u*u*t*p[2] + 3*u*t*t*p[4] + t*t*t*p[6];
            double y = u*u*u*p[1] + 3*u*u*t*p[3] + 3*u*t*t*p[5] + t*t*t*p[7];
            return new double[]{x,y};
        }

        private void drawNodes(Graphics2D g2, Node n) {
            if (n == null) return;
            drawNode(g2, n);
            drawNodes(g2, n.left);
            drawNodes(g2, n.right);
        }

        private void drawNode(Graphics2D g2, Node n) {
            float rawProgress = depthProgress(n.depth);
            if (rawProgress <= 0.02f) return;

            float p = Math.max(0f, Math.min(1f, rawProgress));
            boolean isRoot = (n == root);
            boolean isHover = (n == hoverNode);
            boolean isOnHoverPath = hoverNode != null && containsNode(n, hoverNode);

            double hoverScale = isHover ? 1.06 : 1.0;
            double rootPulse = isRoot ? 0.014 * Math.sin(pulsePhase) : 0;
            double scaleIn = 0.80 + 0.20 * p;
            double finalScale = (hoverScale + rootPulse) * scaleIn;

            double w = NODE_W * finalScale;
            double hh = NODE_H * finalScale;
            double cx = n.x;
            double cy = n.y - (1f - p) * 16;
            int alpha = (int) (255 * p);

            Color base = shiftColor(nodeColor, n.depth);
            Color deep = new Color(
                    Math.max(0, base.getRed() - 55),
                    Math.max(0, base.getGreen() - 55),
                    Math.max(0, base.getBlue() - 55)
            );
            Color cardTop = new Color(45, 46, 55);
            Color cardBottom = new Color(29, 30, 38);

            if (isRoot || isHover || isOnHoverPath) {
                float glow = 0.42f + 0.18f * (float) Math.abs(Math.sin(pulsePhase));
                for (int i = 2; i >= 1; i--) {
                    g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(),
                            (int) ((isRoot ? 42 : (isOnHoverPath ? 34 : 30)) * glow / i * p)));
                    g2.fill(new RoundRectangle2D.Double(cx - w / 2 - i * 3, cy - hh / 2 - i * 3,
                            w + i * 6, hh + i * 6, 14, 14));
                }
            }

            g2.setColor(new Color(0, 0, 0, (int) (120 * p)));
            g2.fill(new RoundRectangle2D.Double(cx - w / 2 + 2, cy - hh / 2 + 5, w, hh, 12, 12));

            RoundRectangle2D body = new RoundRectangle2D.Double(cx - w / 2, cy - hh / 2, w, hh, 12, 12);
            g2.setPaint(new GradientPaint(
                    (float) (cx - w / 2), (float) (cy - hh / 2), withAlpha(cardTop, alpha),
                    (float) (cx + w / 2), (float) (cy + hh / 2), withAlpha(cardBottom, alpha)
            ));
            g2.fill(body);

            double spineW = Math.max(5, 6 * finalScale);
            Shape oldClip = g2.getClip();
            g2.setClip(body);
            g2.setPaint(new GradientPaint(0, (float) (cy - hh / 2), withAlpha(base, alpha),
                    0, (float) (cy + hh / 2), withAlpha(deep, alpha)));
            g2.fill(new Rectangle2D.Double(cx - w / 2, cy - hh / 2, spineW, hh));
            g2.setClip(oldClip);

            g2.setColor(new Color(255, 255, 255, (int) ((isHover ? 95 : 45) * p)));
            g2.setStroke(new BasicStroke(isHover ? 1.6f : 1.05f));
            g2.draw(body);

            g2.setPaint(new GradientPaint(0, (float) (cy - hh / 2 + 1), new Color(255, 255, 255, (int) (24 * p)),
                    0, (float) cy, new Color(255, 255, 255, 0)));
            g2.fill(new RoundRectangle2D.Double(cx - w / 2 + spineW + 1, cy - hh / 2 + 1,
                    w - spineW - 2, hh / 2.0, 12, 12));

            String isbn = trim(key(n.book), 11);
            String title;
            try { title = trim(n.book.getTitle(), 13); } catch (Throwable t) { title = ""; }

            g2.setFont(new Font(FONT_AR, Font.BOLD, Math.max(10, (int) (12 * scaleIn))));
            g2.setColor(new Color(248, 248, 252, alpha));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(isbn, (int) (cx - fm.stringWidth(isbn) / 2.0 + 3), (int) (cy - 4 * finalScale));

            if (!title.isEmpty()) {
                g2.setFont(new Font(FONT_AR, Font.PLAIN, Math.max(8, (int) (9 * scaleIn))));
                g2.setColor(new Color(185, 190, 202, (int) (alpha * 0.92)));
                FontMetrics tfm = g2.getFontMetrics();
                g2.drawString(title, (int) (cx - tfm.stringWidth(title) / 2.0 + 3), (int) (cy + 10 * finalScale));
            }

            String hStr = "h=" + n.height;
            g2.setFont(new Font(FONT_AR, Font.BOLD, Math.max(8, (int) (9 * scaleIn))));
            FontMetrics hfm = g2.getFontMetrics();
            int bw = hfm.stringWidth(hStr) + 10;
            int bh = 13;
            double bx = cx + w / 2 - bw + 3;
            double by = cy - hh / 2 - 6;
            g2.setColor(new Color(8, 10, 14, (int) (185 * p)));
            g2.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 10, 10));
            g2.setColor(withAlpha(base, (int) (alpha * 0.75)));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(bx, by, bw, bh, 10, 10));
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.drawString(hStr, (int) (bx + 5), (int) (by + 10));

            if (isRoot) {
                String rootMark = "ROOT";
                g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(8, (int) (9 * scaleIn))));
                FontMetrics rfm = g2.getFontMetrics();
                int rw = rfm.stringWidth(rootMark) + 12;
                double rx = cx - rw / 2.0;
                double ry = cy - hh / 2 - 16;
                g2.setColor(new Color(WARN.getRed(), WARN.getGreen(), WARN.getBlue(), (int) (34 * p)));
                g2.fill(new RoundRectangle2D.Double(rx, ry, rw, 15, 9, 9));
                g2.setColor(new Color(WARN.getRed(), WARN.getGreen(), WARN.getBlue(), alpha));
                g2.drawString(rootMark, (int) (rx + 6), (int) (ry + 11));
            }
        }

        private Color shiftColor(Color c, int depth) {
            
            int d = Math.min(depth, 5);
            int r = Math.min(255, c.getRed()   + d * 6);
            int g = Math.min(255, c.getGreen() + d * 4);
            int b = Math.min(255, c.getBlue()  + d * 8);
            return new Color(r, g, b);
        }

        private Color withAlpha(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
        }
        private String trim(String s, int max) {
            if (s == null) return "";
            return s.length() <= max ? s : s.substring(0, max - 1) + "…";
        }

        
        private class Particle {
            double x, y, vx, vy, r;
            float alpha;
            Particle() { reset(true); }
            void reset(boolean rand) {
                int w = Math.max(900, getWidth());
                int h = Math.max(500, getHeight());
                x = rnd.nextInt(w);
                y = rand ? rnd.nextInt(h) : h + 10;
                vx = (rnd.nextDouble() - 0.5) * 0.3;
                vy = -0.2 - rnd.nextDouble() * 0.4;
                r = 1.2 + rnd.nextDouble() * 2.2;
                alpha = 0.15f + rnd.nextFloat() * 0.35f;
            }
            void step(int w, int h) {
                x += vx; y += vy;
                if (y < -10 || x < -10 || x > w + 10) reset(false);
            }
            void draw(Graphics2D g2, Color c) {
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 90)));
                g2.fill(new Ellipse2D.Double(x - r, y - r, r*2, r*2));
            }
        }
    }

    
    private class ToggleButton extends JButton {
        private final Color color;
        private boolean selected;
        ToggleButton(String text, Color color) {
            super(text);
            this.color = color;
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setForeground(FG); setFont(new Font(FONT_AR, Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 34));
        }
        public void setSelectedState(boolean s) { this.selected = s; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (selected) {
                g2.setPaint(new GradientPaint(0, 0, color, 0, h,
                        new Color(Math.max(0,color.getRed()-30), Math.max(0,color.getGreen()-30), Math.max(0,color.getBlue()-30))));
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                setForeground(Color.WHITE);
            } else {
                g2.setColor(SURFACE2); g2.fillRoundRect(0, 0, w, h, 12, 12);
                g2.setColor(BORDER_C); g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w-1, h-1, 12, 12);
                setForeground(MUTED);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class GhostButton extends JButton {
        private final Color color;
        GhostButton(String text, Color color) {
            super(text);
            this.color = color;
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setForeground(Color.WHITE); setFont(new Font(FONT_AR, Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 34));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0,0,color,0,getHeight(),
                    new Color(Math.max(0,color.getRed()-30), Math.max(0,color.getGreen()-30), Math.max(0,color.getBlue()-30))));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CompactRoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        CompactRoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius; private final Color color;
        RoundedBorder(int r, Color c) { this.radius = r; this.color = c; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(8,8,8,8); }
    }
}

