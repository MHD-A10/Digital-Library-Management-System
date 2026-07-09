package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;

public class WaitListPanel extends JPanel {
    private static final Color BG        = new Color(18, 18, 22);
    private static final Color SURFACE   = new Color(28, 28, 33);
    private static final Color SURFACE_2 = new Color(44, 44, 52);
    private static final Color BORDER    = new Color(60, 60, 70);
    private static final Color TEXT      = new Color(235, 235, 235);
    private static final Color TEXT_DIM  = new Color(150, 150, 160);
    private static final Color ACCENT    = new Color(74, 158, 255);
    private static final Color SUCCESS   = new Color(70, 180, 110);
    private static final Color WARNING   = new Color(210, 153, 34);
    private static final Color PURPLE    = new Color(163, 113, 247);

    private final LibraryManager library;
    private JTable table;
    private WaitListTableModel model;
    private JTextField searchField;
    private JComboBox<String> bookFilter;
    private String activeFilter = "ALL";

    private StatCard cTotal, cBooks, cHeads, cWaiting;
    private Timer toastTimer;
    private JLabel currentToast;
    private JLabel refreshIcon;
    private Timer refreshSpinTimer;
    private double refreshAngle = 0;
    private final List<Row> allRows = new ArrayList<>();

    public WaitListPanel(LibraryManager library) {
        this.library = library;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        applyComponentOrientation(LanguageManager.orientation());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        refresh();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) refresh();
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);
        header.applyComponentOrientation(LanguageManager.orientation());

        JPanel titleRow = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel icon  = new JLabel("⌛");
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        icon.setForeground(WARNING);
        JLabel title = new JLabel(LanguageManager.text("waitlist.title"));
        title.setFont(new Font("Tahoma", Font.BOLD, 22));
        title.setForeground(TEXT);
        JLabel sub = new JLabel("•  " + LanguageManager.text("waitlist.subtitle"));
        sub.setFont(new Font("Tahoma", Font.PLAIN, 13));
        sub.setForeground(TEXT_DIM);
        titleRow.add(icon);
        titleRow.add(title);
        titleRow.add(sub);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        cTotal   = new StatCard("☰", LanguageManager.text("waitlist.stats.total"), ACCENT);
        cBooks   = new StatCard("●", LanguageManager.text("waitlist.stats.books"), PURPLE);
        cHeads   = new StatCard("✓", LanguageManager.text("waitlist.stats.heads"), SUCCESS);
        cWaiting = new StatCard("⌛", LanguageManager.text("waitlist.stats.waiting"), WARNING);
        stats.add(cTotal); stats.add(cBooks); stats.add(cHeads); stats.add(cWaiting);

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        controls.applyComponentOrientation(LanguageManager.orientation());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(buildRefreshButton());

        JPanel right = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        right.setOpaque(false);
        right.add(buildFilterPill(LanguageManager.text("waitlist.filter.all"), "ALL"));
        right.add(buildFilterPill(LanguageManager.text("waitlist.filter.head"), "HEAD"));
        right.add(buildFilterPill(LanguageManager.text("waitlist.filter.rest"), "REST"));

        controls.add(left, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);
        searchField = new JTextField(22);
        styleField(searchField, LanguageManager.text("waitlist.search"));
        searchField.setComponentOrientation(LanguageManager.orientation());
        searchField.getDocument().addDocumentListener(new SimpleDoc(this::applyFilters));
        bookFilter = new JComboBox<>();
        bookFilter.setBackground(SURFACE_2);
        bookFilter.setForeground(TEXT);
        bookFilter.setFont(new Font("Tahoma", Font.PLAIN, 13));
        bookFilter.applyComponentOrientation(LanguageManager.orientation());
        bookFilter.addActionListener(e -> applyFilters());
        filterBar.add(bookFilter);
        filterBar.add(searchField);

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(titleRow);
        topStack.add(Box.createVerticalStrut(12));
        topStack.add(stats);
        topStack.add(Box.createVerticalStrut(12));

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(controls, BorderLayout.NORTH);
        bottomRow.add(filterBar, BorderLayout.SOUTH);

        header.add(topStack, BorderLayout.NORTH);
        header.add(bottomRow, BorderLayout.SOUTH);
        return header;
    }

    private JComponent buildRefreshButton() {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE_2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 110));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshIcon = new JLabel("↻") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.rotate(refreshAngle, getWidth() / 2.0, getHeight() / 2.0);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        refreshIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        refreshIcon.setForeground(ACCENT);
        JLabel txt = new JLabel(LanguageManager.text("waitlist.refresh"));
        txt.setFont(new Font("Tahoma", Font.BOLD, 13));
        txt.setForeground(TEXT);
        btn.add(refreshIcon);
        btn.add(txt);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent ev) {
                try { SoundManager.play("click"); } catch (Exception ignored) {}
                spinRefresh();
                refresh();
                showToast(LanguageManager.text("waitlist.updated"));
            }
        });
        return btn;
    }

    private void spinRefresh() {
        if (refreshSpinTimer != null && refreshSpinTimer.isRunning()) return;
        final long start = System.currentTimeMillis();
        refreshSpinTimer = new Timer(16, null);
        refreshSpinTimer.addActionListener(e -> {
            long t = System.currentTimeMillis() - start;
            refreshAngle = (t / 500.0) * Math.PI * 2;
            refreshIcon.repaint();
            if (t > 600) {
                refreshAngle = 0;
                refreshIcon.repaint();
                refreshSpinTimer.stop();
            }
        });
        refreshSpinTimer.start();
    }

    private JComponent buildFilterPill(String label, String key) {
        JLabel pill = new JLabel(label, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = key.equals(activeFilter);
                g2.setColor(active ? ACCENT : SURFACE_2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                if (!active) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setForeground(TEXT);
        pill.setFont(new Font("Tahoma", Font.BOLD, 12));
        pill.setBorder(new EmptyBorder(6, 16, 6, 16));
        pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pill.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent ev) {
                try { SoundManager.play("click"); } catch (Exception ignored) {}
                activeFilter = key;
                applyFilters();
                repaint();
            }
        });
        return pill;
    }

    private JComponent buildCenter() {
        model = new WaitListTableModel();
        table = new JTable(model);
        table.setRowHeight(36);
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 60));
        table.setSelectionForeground(TEXT);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setComponentOrientation(LanguageManager.orientation());

        JTableHeader h = table.getTableHeader();
        h.setBackground(SURFACE_2);
        h.setForeground(TEXT);
        h.setFont(new Font("Tahoma", Font.BOLD, 13));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        h.setPreferredSize(new Dimension(0, 36));
        h.setComponentOrientation(LanguageManager.orientation());

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusPillRenderer());
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 5) table.getColumnModel().getColumn(i).setCellRenderer(center);
        }


        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) < 0) {
                    table.clearSelection();
                    if (table.isEditing()) table.getCellEditor().stopCellEditing();
                }
            }
        });        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getViewport().setBackground(SURFACE);
        sp.getViewport().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                table.clearSelection();
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
            }
        });        return sp;
    }

    public void refresh() {
        allRows.clear();
        Set<String> bookSet = new LinkedHashSet<>();
        try {
            for (Book b : library.getBooks()) {
                List<WaitListRequest> queue = library.getWaitListForBook(b.getISBN());
                if (queue == null || queue.isEmpty()) continue;
                bookSet.add(b.getISBN() + "  -  " + b.getTitle());
                int pos = 1;
                for (WaitListRequest req : queue) {
                    Borrower br = req.getBorrower();
                    String brId = br != null ? br.getId() : "-";
                    String name = br != null ? br.getName() : brId;
                    java.time.LocalDateTime when;
                    try { when = req.getRequestTime(); } catch (Throwable t) { when = java.time.LocalDateTime.now(); }
                    if (when == null) when = java.time.LocalDateTime.now();
                    allRows.add(new Row(pos, b.getTitle(), b.getISBN(), name, brId, when, pos == 1));
                    pos++;
                }
            }
        } catch (Exception ignored) {}

        allRows.sort((a, b) -> {
            if (a.head != b.head) return a.head ? -1 : 1;
            int byBook = a.isbn.compareToIgnoreCase(b.isbn);
            if (byBook != 0) return byBook;
            return Integer.compare(a.position, b.position);
        });

        Object selected = bookFilter.getSelectedItem();
        bookFilter.removeAllItems();
        bookFilter.addItem(LanguageManager.text("waitlist.allBooks"));
        for (String s : bookSet) bookFilter.addItem(s);
        if (selected != null) bookFilter.setSelectedItem(selected);

        int total = allRows.size();
        int heads = 0, waiting = 0;
        for (Row r : allRows) { if (r.head) heads++; else waiting++; }
        cTotal.animateTo(total);
        cBooks.animateTo(bookSet.size());
        cHeads.animateTo(heads);
        cWaiting.animateTo(waiting);
        applyFilters();
    }

    private void applyFilters() {
        String q = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String bookSel = bookFilter == null ? LanguageManager.text("waitlist.allBooks") : String.valueOf(bookFilter.getSelectedItem());
        List<Row> filtered = new ArrayList<>();
        for (Row r : allRows) {
            if ("HEAD".equals(activeFilter) && !r.head) continue;
            if ("REST".equals(activeFilter) &&  r.head) continue;
            if (!LanguageManager.text("waitlist.allBooks").equals(bookSel) && !bookSel.startsWith(r.isbn)) continue;
            if (!q.isEmpty()) {
                String hay = (r.name + " " + r.borrowerId).toLowerCase();
                if (!hay.contains(q)) continue;
            }
            filtered.add(r);
        }
        model.setRows(filtered);
    }

    private void showToast(String msg) {
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane == null) return;
        JLayeredPane layer = rootPane.getLayeredPane();

        if (toastTimer != null && toastTimer.isRunning()) {
            toastTimer.stop();
        }
        if (currentToast != null) {
            Container parent = currentToast.getParent();
            if (parent != null) {
                parent.remove(currentToast);
                parent.repaint();
            }
            currentToast = null;
        }

        JLabel toast = new JLabel(msg, SwingConstants.CENTER);
        currentToast = toast;
        toast.setOpaque(true);
        toast.setBackground(SUCCESS.darker());
        toast.setForeground(Color.WHITE);
        toast.setFont(new Font("Tahoma", Font.BOLD, 13));
        toast.setBorder(new EmptyBorder(10, 22, 10, 22));
        Dimension size = toast.getPreferredSize();
        int x = (layer.getWidth() - size.width) / 2;
        toast.setBounds(x, -size.height, size.width, size.height);
        layer.add(toast, JLayeredPane.POPUP_LAYER);
        final int targetY = 18;
        final long start = System.currentTimeMillis();

        toastTimer = new Timer(15, null);
        toastTimer.addActionListener(new ActionListener() {
            int phase = 0;
            long phaseStart = start;
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                if (phase == 0) {
                    float p = Math.min(1f, (now - phaseStart) / 240f);
                    int y = (int) (-size.height + (targetY + size.height) * p);
                    toast.setLocation(toast.getX(), y);
                    if (p >= 1f) { phase = 1; phaseStart = now; }
                } else if (phase == 1) {
                    if (now - phaseStart >= 1300) { phase = 2; phaseStart = now; }
                } else {
                    float p = Math.min(1f, (now - phaseStart) / 260f);
                    int y = (int) (targetY - (targetY + size.height) * p);
                    toast.setLocation(toast.getX(), y);
                    if (p >= 1f) {
                        toastTimer.stop();
                        layer.remove(toast);
                        if (currentToast == toast) currentToast = null;
                        layer.repaint();
                    }
                }
            }
        });
        toastTimer.start();
    }

    private void styleField(JTextField f, String hint) {
        f.setBackground(SURFACE_2);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font("Tahoma", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        f.putClientProperty("JTextField.placeholderText", hint);
    }

    private static class Row {
        int position; String book; String isbn; String name; String borrowerId;
        java.time.LocalDateTime when; boolean head;
        Row(int p, String b, String i, String n, String id, java.time.LocalDateTime w, boolean h) {
            position=p; book=b; isbn=i; name=n; borrowerId=id; when=w; head=h;
        }
    }

    private static class WaitListTableModel extends AbstractTableModel {
        private final String[] cols = {"waitlist.col.position", "waitlist.col.book", "waitlist.col.isbn", "waitlist.col.borrower", "waitlist.col.time", "waitlist.col.status"};
        private List<Row> rows = new ArrayList<>();
        private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        void setRows(List<Row> r) { this.rows = r; fireTableDataChanged(); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return LanguageManager.text(cols[c]); }
        public Object getValueAt(int r, int c) {
            Row x = rows.get(r);
            switch (c) {
                case 0: return "#" + x.position;
                case 1: return x.book;
                case 2: return x.isbn;
                case 3: return x.name + "  (" + x.borrowerId + ")";
                case 4: return x.when.format(FMT);
                case 5: return x.head ? "HEAD" : "WAIT";
                default: return "";
            }
        }
    }

    private static class StatusPillRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            final boolean head = "HEAD".equals(String.valueOf(v));
            JLabel l = new JLabel(head ? LanguageManager.text("waitlist.status.head") : LanguageManager.text("waitlist.status.waiting"), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = head ? new Color(SUCCESS.getRed(), SUCCESS.getGreen(), SUCCESS.getBlue(), 55) : new Color(WARNING.getRed(), WARNING.getGreen(), WARNING.getBlue(), 55);
                    Color br = head ? SUCCESS : WARNING;
                    g2.setColor(bg);
                    g2.fillRoundRect(6, 4, getWidth()-12, getHeight()-8, 14, 14);
                    g2.setColor(br);
                    g2.drawRoundRect(6, 4, getWidth()-13, getHeight()-9, 14, 14);
                    g2.fillOval(12, getHeight()/2-3, 6, 6);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            l.setOpaque(false);
            l.setForeground(TEXT);
            l.setFont(new Font("Tahoma", Font.BOLD, 12));
            return l;
        }
    }

    private static class StatCard extends JPanel {
        private final JLabel valueLbl;
        private final JLabel captionLbl;
        private final JLabel iconLbl;
        private final Color accent;
        private int current = 0;
        private Timer animTimer;
        StatCard(String iconText, String caption, Color accent) {
            this.accent = accent;
            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(14, 16, 14, 16));
            iconLbl = new JLabel(iconText, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            iconLbl.setPreferredSize(new Dimension(44, 44));
            iconLbl.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
            iconLbl.setForeground(accent);
            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            valueLbl = new JLabel("0");
            valueLbl.setFont(new Font("Tahoma", Font.BOLD, 26));
            valueLbl.setForeground(TEXT);
            valueLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            captionLbl = new JLabel(caption);
            captionLbl.setFont(new Font("Tahoma", Font.PLAIN, 12));
            captionLbl.setForeground(TEXT_DIM);
            captionLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            text.add(valueLbl);
            text.add(Box.createVerticalStrut(2));
            text.add(captionLbl);
            add(iconLbl, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SURFACE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            g2.setColor(accent);
            if (LanguageManager.isArabic()) g2.fillRoundRect(getWidth()-4, 12, 4, getHeight()-24, 4, 4);
            else g2.fillRoundRect(0, 12, 4, getHeight()-24, 4, 4);
            g2.dispose();
            super.paintComponent(g);
        }
        void animateTo(int target) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            final int start = current;
            final long t0 = System.currentTimeMillis();
            animTimer = new Timer(16, null);
            animTimer.addActionListener(e -> {
                long dt = System.currentTimeMillis() - t0;
                double p = Math.min(1.0, dt / 450.0);
                p = 1 - Math.pow(1 - p, 3);
                int val = (int) Math.round(start + (target - start) * p);
                current = val;
                valueLbl.setText(String.valueOf(val));
                if (p >= 1.0) animTimer.stop();
            });
            animTimer.start();
        }
    }

    private static class SimpleDoc implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDoc(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}