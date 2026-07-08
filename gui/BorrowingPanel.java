package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class BorrowingPanel extends JPanel {

    
    private static final Color BG       = new Color(18, 18, 22);
    private static final Color SURFACE  = new Color(28, 28, 33);
    private static final Color FG       = new Color(230, 230, 235);
    private static final Color MUTED    = new Color(160, 160, 170);
    private static final Color ACCENT   = new Color(74, 158, 255);
    private static final Color DANGER   = new Color(220, 70, 75);
    private static final Color SUCCESS  = new Color(70, 180, 110);
    private static final Color WARN     = new Color(230, 170, 60);
    private static final Color ROW_ALT  = new Color(34, 34, 40);

    private static final String SYMBOL_FONT = "Segoe UI Symbol";
    private static final String TEXT_FONT   = "Tahoma";

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private enum Filter { ALL, ACTIVE, RETURNED, OVERDUE }

    private final LibraryManager library;
    private final BorrowingTableModel model;
    private final JTable table;
    private final TableRowSorter<BorrowingTableModel> sorter;
    private final JTextField searchField = new JTextField();
    private Filter currentFilter = Filter.ALL;

    
    private StatCard cardTotal, cardActive, cardOverdue, cardReturned;
    
    private FilterPill pillAll, pillActive, pillOverdue, pillReturned;

    
    private int flashRow = -1;
    private float flashAlpha = 0f;

    
    private float pulsePhase = 0f;
    private Timer pulseTimer;

    
    private ToastLayer toastLayer;

    public BorrowingPanel(LibraryManager library) {
        this.library = library;
        this.model   = new BorrowingTableModel();
        this.table   = new JTable(model);
        this.sorter  = new TableRowSorter<>(model);

        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 22, 18, 22));
        applyComponentOrientation(LanguageManager.orientation());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        startPulse();
        refresh();

        SwingUtilities.invokeLater(this::installToastLayer);
        SwingUtilities.invokeLater(this::checkOverdueOnOpen);
    }

    
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setBackground(BG);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(BG);
        JLabel ic = new JLabel("\u21BB");
        ic.setFont(new Font(SYMBOL_FONT, Font.PLAIN, 22));
        ic.setForeground(ACCENT);
        JLabel tt = new JLabel(LanguageManager.text("borrowing.title"));
        tt.setFont(new Font(TEXT_FONT, Font.BOLD, 22));
        tt.setForeground(FG);
        left.add(ic); left.add(tt);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(BG);
        right.add(primaryButton(LanguageManager.text("borrowing.new"), ACCENT, this::onNewBorrow));

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setBackground(BG);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setBackground(BG);
        top.add(buildStatsBar(), BorderLayout.NORTH);
        top.add(buildToolbar(), BorderLayout.CENTER);

        center.add(top, BorderLayout.NORTH);
        center.add(buildTable(), BorderLayout.CENTER);
        return center;
    }

    
    private JPanel buildStatsBar() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        p.setBackground(BG);
        cardTotal    = new StatCard(LanguageManager.text("borrowing.stats.total"), "☰", ACCENT);
        cardActive   = new StatCard(LanguageManager.text("borrowing.stats.active"), "●", SUCCESS);
        cardOverdue  = new StatCard(LanguageManager.text("borrowing.stats.overdue"), "⚠", DANGER);
        cardReturned = new StatCard(LanguageManager.text("borrowing.stats.returned"), "✓", WARN);
        p.add(cardTotal); p.add(cardActive); p.add(cardOverdue); p.add(cardReturned);
        return p;
    }

    
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(BG);

        
        searchField.setColumns(20);
        searchField.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setBackground(SURFACE);
        searchField.setForeground(FG);
        searchField.setCaretColor(FG);
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.putClientProperty("JTextField.placeholderText", LanguageManager.text("borrowing.search"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pills.setBackground(BG);
        pillAll      = new FilterPill(LanguageManager.text("borrowing.filter.all"), ACCENT,  () -> setFilter(Filter.ALL));
        pillActive   = new FilterPill(LanguageManager.text("borrowing.filter.active"), SUCCESS, () -> setFilter(Filter.ACTIVE));
        pillOverdue  = new FilterPill(LanguageManager.text("borrowing.filter.overdue"), DANGER,  () -> setFilter(Filter.OVERDUE));
        pillReturned = new FilterPill(LanguageManager.text("borrowing.filter.returned"), WARN, () -> setFilter(Filter.RETURNED));
        pills.add(pillAll); pills.add(pillActive); pills.add(pillOverdue); pills.add(pillReturned);
        pillAll.setSelected(true);

        bar.add(pills, BorderLayout.WEST);
        bar.add(searchField, BorderLayout.EAST);
        return bar;
    }

    private void setFilter(Filter f) {
        currentFilter = f;
        pillAll.setSelected(f == Filter.ALL);
        pillActive.setSelected(f == Filter.ACTIVE);
        pillOverdue.setSelected(f == Filter.OVERDUE);
        pillReturned.setSelected(f == Filter.RETURNED);
        SoundManager.play("click");
        applyFilter();
    }

    
    private JScrollPane buildTable() {
        table.setRowHeight(40);
        table.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        table.setBackground(SURFACE);
        table.setForeground(FG);
        table.setGridColor(new Color(45, 45, 52));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setComponentOrientation(LanguageManager.orientation());

        table.getTableHeader().setFont(new Font(TEXT_FONT, Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(40, 40, 48));
        table.getTableHeader().setForeground(FG);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                int modelRow = table.convertRowIndexToModel(row);
                BorrowRecord rec = model.getRecordAt(modelRow);
                boolean overdue = rec != null && !rec.isReturned()
                        && rec.getExpectedReturnDate().isBefore(LocalDate.now());

                if (sel) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else if (row == flashRow) {
                    Color base = (row % 2 == 0 ? SURFACE : ROW_ALT);
                    float a = Math.max(0f, Math.min(1f, flashAlpha));
                    int r  = (int) (base.getRed()   * (1 - a) + ACCENT.getRed()   * a);
                    int g  = (int) (base.getGreen() * (1 - a) + ACCENT.getGreen() * a);
                    int bl = (int) (base.getBlue()  * (1 - a) + ACCENT.getBlue()  * a);
                    c.setBackground(new Color(r, g, bl));
                    c.setForeground(FG);
                } else {
                    c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                    c.setForeground(overdue ? DANGER : FG);
                }
                setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusPillRenderer());

        
        table.getColumnModel().getColumn(7).setCellRenderer(new ReturnButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ReturnButtonEditor());
        table.getColumnModel().getColumn(7).setMinWidth(110);
        table.getColumnModel().getColumn(7).setMaxWidth(140);


        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) < 0) {
                    table.clearSelection();
                    if (table.isEditing()) table.getCellEditor().stopCellEditing();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 52), 1, true));
        sp.getViewport().setBackground(SURFACE);
        sp.getViewport().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                table.clearSelection();
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
            }
        });
        return sp;
    }

    private void startPulse() {
        pulseTimer = new Timer(40, e -> {
            pulsePhase += 0.09f;
            if (pulsePhase > Math.PI * 2) pulsePhase -= (float) (Math.PI * 2);
            int col = 6;
            if (table.getColumnCount() > col) {
                Rectangle r = table.getCellRect(0, col, true);
                r.height = table.getHeight();
                table.repaint(r);
            }
        });
        pulseTimer.start();
    }

    private void flashRecordRow(String recordId) {
        int target = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (recordId.equals(model.getValueAt(i, 0))) { target = i; break; }
        }
        if (target < 0) return;
        int viewRow;
        try { viewRow = table.convertRowIndexToView(target); }
        catch (IndexOutOfBoundsException ex) { return; }
        if (viewRow < 0) return;

        flashRow = viewRow;
        flashAlpha = 0.55f;
        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            flashAlpha -= 0.04f;
            if (flashAlpha <= 0f) { flashAlpha = 0f; flashRow = -1; ((Timer) e.getSource()).stop(); }
            table.repaint();
        });
        t.start();
    }

    
    private void onNewBorrow(ActionEvent e) {
        BorrowFormDialog dlg = new BorrowFormDialog((JFrame) SwingUtilities.getWindowAncestor(this), library);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            SoundManager.play("success");
            showToast(LanguageManager.text("borrowing.borrow.success"), SUCCESS);
            refresh();
        }
    }

    private void doReturn(BorrowRecord rec) {
        if (rec == null) return;
        if (rec.isReturned()) {
            SoundManager.play("error");
            showToast(LanguageManager.text("borrowing.alreadyReturned"), WARN);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                LanguageManager.text("borrowing.return.confirm") + " " + rec.getBook().getTitle() + " ?",
                LanguageManager.text("borrowing.return.confirmTitle"), JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean done = library.returnBook(rec.getRecordId());
        if (done) {
            SoundManager.play("success");
            showToast(LanguageManager.text("borrowing.return.success"), SUCCESS);
            String id = rec.getRecordId();
            refresh();
            flashRecordRow(id);
        } else {
            SoundManager.play("error");
            showToast(LanguageManager.text("borrowing.return.fail"), DANGER);
        }
    }

    private void applyFilter() {
        final String q = searchField.getText().trim().toLowerCase();
        final Filter f = currentFilter;
        sorter.setRowFilter(new RowFilter<BorrowingTableModel, Integer>() {
            @Override public boolean include(Entry<? extends BorrowingTableModel, ? extends Integer> entry) {
                BorrowRecord r = model.getRecordAt(entry.getIdentifier());
                if (r == null) return false;
                boolean overdue = !r.isReturned() && r.getExpectedReturnDate().isBefore(LocalDate.now());
                switch (f) {
                    case ACTIVE:   if (r.isReturned() || overdue) return false; break;
                    case RETURNED: if (!r.isReturned()) return false; break;
                    case OVERDUE:  if (!overdue) return false; break;
                    default:
                }
                if (q.isEmpty()) return true;
                String hay = (r.getRecordId() + " " + r.getBorrower().getName() + " "
                        + r.getBorrower().getId() + " " + r.getBook().getTitle() + " "
                        + r.getBook().getISBN()).toLowerCase();
                return hay.contains(q);
            }
        });
    }

    public void refresh() {
        model.setRecords(library.getAllBorrowRecords());
        updateStats();
        applyFilter();
    }

    private void updateStats() {
        List<BorrowRecord> all = library.getAllBorrowRecords();
        int total = all.size(), active = 0, overdue = 0, returned = 0;
        for (BorrowRecord r : all) {
            if (r.isReturned()) returned++;
            else if (r.getExpectedReturnDate().isBefore(LocalDate.now())) overdue++;
            else active++;
        }
        if (cardTotal    != null) cardTotal.setTarget(total);
        if (cardActive   != null) cardActive.setTarget(active);
        if (cardOverdue  != null) cardOverdue.setTarget(overdue);
        if (cardReturned != null) cardReturned.setTarget(returned);
    }

    private void checkOverdueOnOpen() {
        int overdue = 0;
        for (BorrowRecord r : library.getAllBorrowRecords()) {
            if (!r.isReturned() && r.getExpectedReturnDate().isBefore(LocalDate.now())) overdue++;
        }
        if (overdue > 0) {
            SoundManager.play("error");
            showToast(LanguageManager.text("borrowing.overdueAlert.prefix") + " " + overdue + " " + LanguageManager.text("borrowing.overdueAlert.suffix"), DANGER);
        }
    }

    
    private void installToastLayer() {
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp == null) return;
        if (toastLayer == null) {
            toastLayer = new ToastLayer();
            JLayeredPane lp = rp.getLayeredPane();
            lp.add(toastLayer, JLayeredPane.POPUP_LAYER);
            toastLayer.setBounds(0, 0, lp.getWidth(), lp.getHeight());
            lp.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override public void componentResized(java.awt.event.ComponentEvent e) {
                    toastLayer.setBounds(0, 0, lp.getWidth(), lp.getHeight());
                }
            });
        }
    }

    private void showToast(String msg, Color color) {
        if (toastLayer == null) installToastLayer();
        if (toastLayer == null) {
            JOptionPane.showMessageDialog(this, msg);
            return;
        }
        toastLayer.show(msg, color);
    }

    

    
    private JButton primaryButton(String text, Color bg, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(new Font(TEXT_FONT, Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> { SoundManager.play("click"); listener.actionPerformed(e); });
        return b;
    }

    
    private class StatCard extends JPanel {
        private final JLabel valueLbl;
        private int current = 0, target = 0;
        private final Color color;
        StatCard(String title, String icon, Color color) {
            this.color = color;
            setLayout(new BorderLayout(0, 4));
            setBackground(SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90), 1, true),
                    new EmptyBorder(12, 14, 12, 14)));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            JLabel ic = new JLabel(icon);
            ic.setFont(new Font(SYMBOL_FONT, Font.PLAIN, 20));
            ic.setForeground(color);
            valueLbl = new JLabel("0");
            valueLbl.setFont(new Font(TEXT_FONT, Font.BOLD, 26));
            valueLbl.setForeground(FG);
            top.add(ic, BorderLayout.WEST);
            top.add(valueLbl, BorderLayout.EAST);

            JLabel t = new JLabel(title);
            t.setFont(new Font(TEXT_FONT, Font.PLAIN, 12));
            t.setForeground(MUTED);
            t.setHorizontalAlignment(SwingConstants.RIGHT);

            add(top, BorderLayout.NORTH);
            add(t, BorderLayout.SOUTH);
        }
        void setTarget(int v) {
            this.target = v;
            Timer t = new Timer(20, null);
            t.addActionListener(e -> {
                if (current < target) current = Math.min(target, current + Math.max(1, (target - current) / 5));
                else if (current > target) current = Math.max(target, current - Math.max(1, (current - target) / 5));
                valueLbl.setText(String.valueOf(current));
                if (current == target) ((Timer) e.getSource()).stop();
            });
            t.start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 35),
                    0, getHeight(), SURFACE);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
        }
    }

    
    private class FilterPill extends JButton {
        private final Color color;
        private boolean selected = false;
        FilterPill(String text, Color color, Runnable onClick) {
            super(text);
            this.color = color;
            setFont(new Font(TEXT_FONT, Font.BOLD, 12));
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 16, 6, 16));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setForeground(FG);
            addActionListener(e -> onClick.run());
        }
        public void setSelected(boolean s) { this.selected = s; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = selected ? color
                    : new Color(color.getRed(), color.getGreen(), color.getBlue(), 40);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), selected ? 255 : 140));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
            g2.dispose();
            setForeground(selected ? Color.WHITE : color);
            super.paintComponent(g);
        }
    }

    
    private class StatusPillRenderer extends JPanel implements TableCellRenderer {
        private String text = "";
        private Color color = SUCCESS;
        private boolean selected = false;
        private boolean animated = false;
        StatusPillRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foc, int row, int col) {
            String s = v == null ? "" : v.toString();
            this.text = s;
            this.selected = sel;
            if (s.equals(LanguageManager.text("borrowing.status.overdue")))      { color = DANGER;  animated = true; }
            else if (s.equals(LanguageManager.text("borrowing.status.returned"))) { color = WARN; animated = false; }
            else                            { color = SUCCESS; animated = false; }
            setBackground(sel ? ACCENT : (row % 2 == 0 ? SURFACE : ROW_ALT));
            return this;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font(TEXT_FONT, Font.BOLD, 12);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int dot = 8, gap = 6, padH = 12, padV = 6;
            int textW = fm.stringWidth(text);
            int pillW = padH * 2 + dot + gap + textW;
            int pillH = fm.getHeight() + padV;
            int x = (getWidth() - pillW) / 2;
            int y = (getHeight() - pillH) / 2;

            float bgAlpha = selected ? 0.35f : 0.18f;
            if (animated) {
                float pulse = (float) (0.5f + 0.5f * Math.sin(pulsePhase));
                bgAlpha = 0.15f + 0.20f * pulse;
            }
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(255, (int) (bgAlpha * 255))));
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 140));
            g2.drawRoundRect(x, y, pillW - 1, pillH - 1, pillH, pillH);

            int dotX = x + padH;
            int dotY = y + (pillH - dot) / 2;
            if (animated) {
                float pulse = (float) (0.5f + 0.5f * Math.sin(pulsePhase));
                int halo = (int) (dot + 8 * pulse);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                        (int) (90 * (1 - pulse))));
                g2.fillOval(dotX - (halo - dot) / 2, dotY - (halo - dot) / 2, halo, halo);
            }
            g2.setColor(color);
            g2.fillOval(dotX, dotY, dot, dot);

            g2.setColor(selected ? Color.WHITE : color.brighter());
            int tx = dotX + dot + gap;
            int ty = y + (pillH - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    
    private class ReturnButtonRenderer extends JPanel implements TableCellRenderer {
        private final JLabel icon = new JLabel("\u21A9");
        private final JLabel txt  = new JLabel(LanguageManager.text("borrowing.action.return"));
        private final JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        ReturnButtonRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            icon.setFont(new Font(SYMBOL_FONT, Font.PLAIN, 13));
            icon.setForeground(Color.WHITE);
            txt.setFont(new Font(TEXT_FONT, Font.BOLD, 12));
            txt.setForeground(Color.WHITE);
            pill.setOpaque(false);
            pill.setBackground(ACCENT);
            pill.setBorder(new EmptyBorder(4, 12, 4, 12));
            pill.add(icon); pill.add(txt);
            add(pill);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean foc, int row, int col) {
            int modelRow = table.convertRowIndexToModel(row);
            BorrowRecord rec = model.getRecordAt(modelRow);
            boolean canReturn = rec != null && !rec.isReturned();
            setBackground(sel ? ACCENT : (row % 2 == 0 ? SURFACE : ROW_ALT));
            if (canReturn) {
                pill.setBackground(ACCENT);
                pill.setVisible(true);
            } else {
                pill.setVisible(false);
            }
            return this;
        }
    }

    
    private class ReturnButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel host = new JPanel(new GridBagLayout());
        private final JLabel icon = new JLabel("\u21A9");
        private final JLabel txt  = new JLabel(LanguageManager.text("borrowing.action.return"));
        private final JPanel pill = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        private BorrowRecord current;
        ReturnButtonEditor() {
            host.setOpaque(true);
            icon.setFont(new Font(SYMBOL_FONT, Font.PLAIN, 13));
            icon.setForeground(Color.WHITE);
            txt.setFont(new Font(TEXT_FONT, Font.BOLD, 12));
            txt.setForeground(Color.WHITE);
            pill.setOpaque(false);
            pill.setBackground(ACCENT);
            pill.setBorder(new EmptyBorder(4, 12, 4, 12));
            pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            pill.add(icon); pill.add(txt);
            host.add(pill);
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { pill.setBackground(ACCENT.brighter()); }
                @Override public void mouseExited(MouseEvent e)  { pill.setBackground(ACCENT); }
                @Override public void mousePressed(MouseEvent e) {
                    SoundManager.play("click");
                    BorrowRecord rec = current;
                    fireEditingStopped();
                    doReturn(rec);
                }
            };
            pill.addMouseListener(ma);
            icon.addMouseListener(ma);
            txt.addMouseListener(ma);
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int row, int col) {
            int modelRow = table.convertRowIndexToModel(row);
            current = model.getRecordAt(modelRow);
            host.setBackground(sel ? ACCENT : (row % 2 == 0 ? SURFACE : ROW_ALT));
            pill.setVisible(current != null && !current.isReturned());
            return host;
        }
        @Override public Object getCellEditorValue() { return ""; }
        @Override public boolean isCellEditable(EventObject e) { return true; }
    }

    
    private class ToastLayer extends JPanel {
        private String message = "";
        private Color color = ACCENT;
        private float alpha = 0f;
        private int yOffset = -40;
        private Timer in, hold, out;
        ToastLayer() { setOpaque(false); setLayout(null); }
        void show(String msg, Color c) {
            this.message = msg; this.color = c;
            if (in != null && in.isRunning()) in.stop();
            if (hold != null && hold.isRunning()) hold.stop();
            if (out != null && out.isRunning()) out.stop();
            alpha = 0f; yOffset = -40;
            in = new Timer(15, e -> {
                alpha = Math.min(1f, alpha + 0.08f);
                yOffset = Math.min(20, yOffset + 6);
                repaint();
                if (alpha >= 1f && yOffset >= 20) { ((Timer) e.getSource()).stop(); startHold(); }
            });
            in.start();
        }
        private void startHold() {
            hold = new Timer(1800, e -> { ((Timer) e.getSource()).stop(); startOut(); });
            hold.setRepeats(false); hold.start();
        }
        private void startOut() {
            out = new Timer(15, e -> {
                alpha = Math.max(0f, alpha - 0.06f);
                yOffset -= 4;
                repaint();
                if (alpha <= 0f) ((Timer) e.getSource()).stop();
            });
            out.start();
        }
        @Override protected void paintComponent(Graphics g) {
            if (alpha <= 0f || message.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font f = new Font(TEXT_FONT, Font.BOLD, 13);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int padH = 18, padV = 12;
            int w = fm.stringWidth(message) + padH * 2;
            int h = fm.getHeight() + padV;
            int x = (getWidth() - w) / 2;
            int y = yOffset;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x + 2, y + 4, w, h, h, h);
            g2.setColor(SURFACE);
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w - 1, h - 1, h, h);
            g2.setColor(FG);
            g2.drawString(message, x + padH, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    
    private static class BorrowingTableModel extends AbstractTableModel {
        private final String[] cols = {"borrowing.col.record", "borrowing.col.borrower", "borrowing.col.book", "books.col.isbn", "borrowing.col.borrowDate", "borrowing.col.dueDate", "borrowing.col.status", "borrowing.col.action"};
        private List<BorrowRecord> records = new ArrayList<>();
        void setRecords(List<BorrowRecord> r) { this.records = r; fireTableDataChanged(); }
        BorrowRecord getRecordAt(int row) {
            if (row < 0 || row >= records.size()) return null;
            return records.get(row);
        }
        public int getRowCount() { return records.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return LanguageManager.text(cols[c]); }
        @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        public Object getValueAt(int r, int c) {
            BorrowRecord rec = records.get(r);
            switch (c) {
                case 0: return rec.getRecordId();
                case 1: return rec.getBorrower().getName() + " (#" + rec.getBorrower().getId() + ")";
                case 2: return rec.getBook().getTitle();
                case 3: return rec.getBook().getISBN();
                case 4: return rec.getBorrowDate().format(DF);
                case 5: return rec.getExpectedReturnDate().format(DF);
                case 6: return rec.isReturned() ? LanguageManager.text("borrowing.status.returned")
                        : (rec.getExpectedReturnDate().isBefore(LocalDate.now()) ? LanguageManager.text("borrowing.status.overdue") : LanguageManager.text("borrowing.status.active"));
                case 7: return "";
                default: return "";
            }
        }
    }
}
