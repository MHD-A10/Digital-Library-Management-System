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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BooksPanel extends JPanel {

    private static final Color BG       = new Color(18, 18, 22);
    private static final Color SURFACE  = new Color(28, 28, 33);
    private static final Color FG       = new Color(230, 230, 235);
    private static final Color MUTED    = new Color(160, 160, 170);
    private static final Color ACCENT   = new Color(74, 158, 255);
    private static final Color DANGER   = new Color(220, 70, 75);
    private static final Color SUCCESS  = new Color(70, 180, 110);
    private static final Color TREE     = SUCCESS;
    private static final Color ROW_ALT  = new Color(34, 34, 40);

    private static final String EMOJI_FONT = "Segoe UI Emoji";
    private static final String TEXT_FONT  = "Tahoma";

    private enum Filter { ALL, ISBN, TITLE, AUTHOR }

    private final LibraryManager library;
    private final BooksTableModel model;
    private final JTable table;
    private final TableRowSorter<BooksTableModel> sorter;
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> filterCombo = new JComboBox<>();
    private final JLabel statsLabel = new JLabel();

    private int flashRow = -1;
    private float flashAlpha = 0f;

    public BooksPanel(LibraryManager library) {
        this.library = library;
        this.model   = new BooksTableModel();
        this.table   = new JTable(model);
        this.sorter  = new TableRowSorter<>(model);

        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(18, 22, 18, 22));
        applyComponentOrientation(LanguageManager.orientation());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setBackground(BG);
        header.applyComponentOrientation(LanguageManager.orientation());

        JPanel titleWrap = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        titleWrap.setBackground(BG);
        JLabel titleIcon = new JLabel("\uD83D\uDCDA");
        titleIcon.setFont(new Font(EMOJI_FONT, Font.PLAIN, 22));
        titleIcon.setForeground(FG);
        JLabel titleText = new JLabel(LanguageManager.text("books.title"));
        titleText.setFont(new Font(TEXT_FONT, Font.BOLD, 22));
        titleText.setForeground(FG);
        titleWrap.add(titleIcon);
        titleWrap.add(titleText);

        JPanel toolbar = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(BG);
        toolbar.applyComponentOrientation(LanguageManager.orientation());

        setupFilterCombo();
        setupSearchField();

        toolbar.add(filterCombo);
        toolbar.add(searchField);
        toolbar.add(iconButton("\u2795", LanguageManager.text("books.add"), ACCENT, "click", this::onAdd));
        toolbar.add(iconButton("\u270F", LanguageManager.text("books.updateCopies"), SURFACE, "click", this::onUpdateCopies));
        toolbar.add(iconButton("\uD83D\uDDD1", LanguageManager.text("books.delete"), DANGER, "delete", this::onDelete));
        toolbar.add(iconButton("\uD83C\uDF33", LanguageManager.text("books.tree"), TREE, "click", this::onShowTree));
        toolbar.add(iconButton("\uD83D\uDD04", LanguageManager.text("books.refresh"), SURFACE, "click",
                e -> { refresh(); showTopToast(LanguageManager.text("books.refreshed"), SUCCESS); }));

        header.add(titleWrap, BorderLayout.NORTH);
        header.add(toolbar, BorderLayout.CENTER);
        return header;
    }

    private void setupFilterCombo() {
        filterCombo.removeAllItems();
        filterCombo.addItem(LanguageManager.text("books.filter.all"));
        filterCombo.addItem("ISBN");
        filterCombo.addItem(LanguageManager.text("books.filter.title"));
        filterCombo.addItem(LanguageManager.text("books.filter.author"));
        filterCombo.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        filterCombo.setBackground(SURFACE);
        filterCombo.setForeground(FG);
        filterCombo.setPreferredSize(new Dimension(120, 34));
        filterCombo.applyComponentOrientation(LanguageManager.orientation());
        filterCombo.addActionListener(e -> { updateSearchPlaceholder(); applyFilter(); });
    }

    private void setupSearchField() {
        searchField.setColumns(18);
        searchField.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setBackground(SURFACE);
        searchField.setForeground(FG);
        searchField.setCaretColor(FG);
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.setMinimumSize(new Dimension(160, 34));
        searchField.setComponentOrientation(LanguageManager.orientation());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        updateSearchPlaceholder();
    }

    private Filter currentFilter() {
        switch (filterCombo.getSelectedIndex()) {
            case 1:  return Filter.ISBN;
            case 2:  return Filter.TITLE;
            case 3:  return Filter.AUTHOR;
            default: return Filter.ALL;
        }
    }

    private void updateSearchPlaceholder() {
        String hint;
        switch (currentFilter()) {
            case ISBN:   hint = LanguageManager.text("books.search.isbn"); break;
            case TITLE:  hint = LanguageManager.text("books.search.title"); break;
            case AUTHOR: hint = LanguageManager.text("books.search.author"); break;
            default:     hint = LanguageManager.text("books.search.all"); break;
        }
        searchField.putClientProperty("JTextField.placeholderText", hint);
        searchField.repaint();
    }

    private JButton iconButton(String emoji, String text, Color bg, String sound,
                               java.awt.event.ActionListener listener) {
        JButton b = new JButton() {
            private float rippleAlpha = 0f;
            private int rippleX, rippleY, rippleRadius;
            private Timer rippleTimer;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        rippleX = e.getX(); rippleY = e.getY();
                        rippleRadius = 0; rippleAlpha = 0.45f;
                        if (rippleTimer != null && rippleTimer.isRunning()) rippleTimer.stop();
                        int max = Math.max(getWidth(), getHeight()) * 2;
                        rippleTimer = new Timer(15, ev -> {
                            rippleRadius += 8;
                            rippleAlpha -= 0.025f;
                            if (rippleRadius >= max || rippleAlpha <= 0f) {
                                rippleAlpha = 0f;
                                ((Timer) ev.getSource()).stop();
                            }
                            repaint();
                        });
                        rippleTimer.start();
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (rippleAlpha > 0f) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(1f, 1f, 1f, rippleAlpha));
                    g2.fillOval(rippleX - rippleRadius, rippleY - rippleRadius,
                            rippleRadius * 2, rippleRadius * 2);
                    g2.dispose();
                }
            }
        };
        b.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.applyComponentOrientation(LanguageManager.orientation());

        Color fg = bg.equals(SURFACE) ? FG : Color.WHITE;
        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font(EMOJI_FONT, Font.PLAIN, 13));
        icon.setForeground(fg);
        JLabel txt = new JLabel(text);
        txt.setFont(new Font(TEXT_FONT, Font.BOLD, 12));
        txt.setForeground(fg);
        b.add(icon);
        b.add(txt);

        b.addActionListener(e -> { SoundManager.play(sound); listener.actionPerformed(e); });
        return b;
    }

    private JScrollPane buildTable() {
        table.setRowHeight(34);
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
        table.getTableHeader().setComponentOrientation(LanguageManager.orientation());

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                } else if (row == flashRow) {
                    Color base = (row % 2 == 0 ? SURFACE : ROW_ALT);
                    float a = Math.max(0f, Math.min(1f, flashAlpha));
                    int r = (int) (base.getRed()   * (1 - a) + 255 * a);
                    int g = (int) (base.getGreen() * (1 - a) + 230 * a);
                    int bl = (int) (base.getBlue() * (1 - a) + 90  * a);
                    c.setBackground(new Color(r, g, bl));
                    c.setForeground(FG);
                } else {
                    c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                    c.setForeground(FG);
                }
                int textAlignment = LanguageManager.isArabic() ? RIGHT : LEFT;
                setHorizontalAlignment(col >= 3 ? CENTER : textAlignment);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }


        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) < 0) {
                    table.clearSelection();
                    if (table.isEditing()) table.getCellEditor().stopCellEditing();
                }
            }
        });        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 52), 1, true));
        sp.getViewport().setBackground(SURFACE);
        sp.getViewport().addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                table.clearSelection();
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
            }
        });        return sp;
    }

    private void flashBookRow(String isbn) {
        int target = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (isbn.equals(model.getValueAt(i, 0))) { target = i; break; }
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
            if (flashAlpha <= 0f) {
                flashAlpha = 0f; flashRow = -1;
                ((Timer) e.getSource()).stop();
            }
            table.repaint();
        });
        t.start();
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.applyComponentOrientation(LanguageManager.orientation());
        statsLabel.setForeground(MUTED);
        statsLabel.setFont(new Font(TEXT_FONT, Font.PLAIN, 12));
        statsLabel.setBorder(new EmptyBorder(8, 4, 0, 4));
        footer.add(statsLabel, LanguageManager.isArabic() ? BorderLayout.WEST : BorderLayout.EAST);
        return footer;
    }

    private void onAdd(java.awt.event.ActionEvent e) {
        BookFormDialog dlg = new BookFormDialog(SwingUtilities.getWindowAncestor(this));
        BookFormDialog.Result r = dlg.showDialog();
        if (r == null) return;

        boolean ok = library.addBook(r.isbn, r.title, r.author, r.copies);
        if (ok) {
            SoundManager.play("success");
            toast(LanguageManager.text("books.add.success"), SUCCESS);
            refresh();
            flashBookRow(r.isbn);
        } else {
            SoundManager.play("error");
            toast(LanguageManager.text("books.add.fail"), DANGER);
        }
    }

    private void onUpdateCopies(java.awt.event.ActionEvent e) {
        Book book = selectedBook();
        if (book == null) {
            SoundManager.play("error");
            toast(LanguageManager.text("books.selectFirst"), DANGER);
            return;
        }

        JSpinner spin = new JSpinner(new SpinnerNumberModel(book.getTotalCopies(), 0, 9999, 1));
        int res = JOptionPane.showConfirmDialog(this, spin,
                LanguageManager.text("books.update.title") + " - " + book.getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) { SoundManager.play("back"); return; }

        int newTotal = (Integer) spin.getValue();
        if (library.updateBookCopies(book.getISBN(), newTotal)) {
            SoundManager.play("success");
            toast(LanguageManager.text("books.update.success"), SUCCESS);
            refresh();
            flashBookRow(book.getISBN());
        } else {
            SoundManager.play("error");
            toast(LanguageManager.text("books.update.fail"), DANGER);
        }
    }

    private void onDelete(java.awt.event.ActionEvent e) {
        Book book = selectedBook();
        if (book == null) {
            SoundManager.play("error");
            toast(LanguageManager.text("books.selectFirst"), DANGER);
            return;
        }

        SoundManager.play("warning");
        int res = JOptionPane.showConfirmDialog(this,
                LanguageManager.text("books.delete.confirm") + "\n" + book.getTitle() + " (" + book.getISBN() + ") ?",
                LanguageManager.text("books.delete.confirm.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) { SoundManager.play("back"); return; }

        if (library.deleteBook(book.getISBN())) {
            SoundManager.play("delete");
            toast(LanguageManager.text("books.delete.success"), SUCCESS);
            refresh();
        } else {
            SoundManager.play("error");
            toast(LanguageManager.text("books.delete.fail"), DANGER);
        }
    }

    private void onShowTree(java.awt.event.ActionEvent e) {
        AVLVisualizerDialog dlg = new AVLVisualizerDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), library);
        dlg.setVisible(true);
    }

    private Book selectedBook() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.getBookAt(modelRow);
    }

    private void applyFilter() {
        String q = searchField.getText().trim();
        Filter f = currentFilter();

        if (f == Filter.ISBN && !q.isEmpty() && !q.matches("[\\d-]+")) {
            sorter.setRowFilter(new RowFilter<BooksTableModel, Integer>() {
                public boolean include(Entry<? extends BooksTableModel, ? extends Integer> entry) { return false; }
            });
            return;
        }

        if (q.isEmpty()) { sorter.setRowFilter(null); return; }

        String pattern = "(?i)" + java.util.regex.Pattern.quote(q);
        int[] cols;
        switch (f) {
            case ISBN:   cols = new int[]{0}; break;
            case TITLE:  cols = new int[]{1}; break;
            case AUTHOR: cols = new int[]{2}; break;
            default:     cols = new int[]{0, 1, 2};
        }
        sorter.setRowFilter(RowFilter.regexFilter(pattern, cols));
    }

    public void refresh() {
        model.setBooks(library.getBooks());
        updateStats();
    }

    private void updateStats() {
        List<Book> all = library.getBooks();
        int total = 0, available = 0;
        for (Book b : all) { total += b.getTotalCopies(); available += b.getAvailableCopies(); }
        String separator = "   •   ";
        statsLabel.setText(LanguageManager.text("books.stats.titles") + ": " + all.size()
                + separator + LanguageManager.text("books.stats.copies") + ": " + total
                + separator + LanguageManager.text("books.stats.available") + ": " + available
                + separator + LanguageManager.text("books.stats.borrowed") + ": " + (total - available));
    }

    private void showTopToast(String msg, Color color) {
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane == null) {
            return;
        }

        JLayeredPane layer = rootPane.getLayeredPane();
        JLabel toast = new JLabel(msg, SwingConstants.CENTER);
        toast.setOpaque(true);
        toast.setBackground(color.darker());
        toast.setForeground(Color.WHITE);
        toast.setFont(new Font(TEXT_FONT, Font.BOLD, 13));
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

    private void toast(String msg, Color color) {
        JOptionPane.showMessageDialog(this, msg, LanguageManager.text("books.notification"),
                color == SUCCESS ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private static class BooksTableModel extends AbstractTableModel {
        private List<Book> books = new ArrayList<>();

        void setBooks(List<Book> b) { this.books = b; fireTableDataChanged(); }
        Book getBookAt(int row) { return books.get(row); }

        public int getRowCount() { return books.size(); }
        public int getColumnCount() { return 7; }
        public String getColumnName(int c) {
            switch (c) {
                case 0: return LanguageManager.text("books.col.isbn");
                case 1: return LanguageManager.text("books.col.title");
                case 2: return LanguageManager.text("books.col.author");
                case 3: return LanguageManager.text("books.col.total");
                case 4: return LanguageManager.text("books.col.available");
                case 5: return LanguageManager.text("books.col.borrowed");
                case 6: return LanguageManager.text("books.col.borrowCount");
                default: return "";
            }
        }

        public Object getValueAt(int r, int c) {
            Book b = books.get(r);
            switch (c) {
                case 0: return b.getISBN();
                case 1: return b.getTitle();
                case 2: return b.getAuthorName();
                case 3: return b.getTotalCopies();
                case 4: return b.getAvailableCopies();
                case 5: return b.getTotalCopies() - b.getAvailableCopies();
                case 6: return b.getBorrowCount();
                default: return "";
            }
        }

        public Class<?> getColumnClass(int c) { return (c >= 3) ? Integer.class : String.class; }
    }

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false);
            d.width -= (getHgap() + 1);
            return d;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                int n = target.getComponentCount();

                for (int i = 0; i < n; i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0; rowHeight = 0;
                    }
                    if (rowWidth != 0) rowWidth += hgap;
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                addRow(dim, rowWidth, rowHeight);

                dim.width  += insets.left + insets.right + hgap * 2;
                dim.height += insets.top + insets.bottom + vgap * 2;

                Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                if (scroll != null && target.isValid()) dim.width -= (hgap + 1);
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowHeight;
        }
    }
}