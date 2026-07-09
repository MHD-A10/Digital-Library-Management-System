package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;




public class ReportsPanel extends JPanel {

    
    private static final Color BG          = new Color(18, 18, 22);
    private static final Color CARD        = new Color(28, 28, 34);
    private static final Color BORDER      = new Color(45, 45, 55);
    private static final Color TEXT        = new Color(235, 235, 240);
    private static final Color TEXT_DIM    = new Color(160, 160, 170);
    private static final Color ACCENT      = new Color(74, 158, 255);
    private static final Color SUCCESS     = new Color(76, 209, 130);
    private static final Color WARNING     = new Color(255, 176, 60);
    private static final Color DANGER      = new Color(255, 95, 95);
    private static final Color PURPLE      = new Color(170, 120, 255);
    private static final Color ACTION_SECONDARY = new Color(34, 34, 42);

    private final LibraryManager library;
    private final DataPersistence dataPersistence = new DataPersistence();
    private final ReportsManager reports = new ReportsManager();

    private StatCard cardBooks, cardBorrowers, cardRecords, cardOverdue;
    private JTextArea reportArea;
    private BarChartPanel barChart;
    private PieChartPanel pieChart;
    private JSplitPane mainSplit;
    private boolean resizeRefreshQueued;
    private JLabel toast;
    private Timer toastTimer;

    public ReportsPanel(LibraryManager library) {
        this.library = library;
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        toast = new JLabel("", SwingConstants.CENTER);
        toast.setOpaque(true);
        toast.setBackground(new Color(40, 40, 50));
        toast.setForeground(TEXT);
        toast.setFont(new Font("Tahoma", Font.BOLD, 13));
        toast.setBorder(new EmptyBorder(10, 22, 10, 22));
        toast.setVisible(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshResponsiveLayout();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                refreshResponsiveLayout();
            }
        });

        SwingUtilities.invokeLater(() -> {
            refresh();
            refreshResponsiveLayout();
        });
    }

    private static String tr(String key) {
        return LanguageManager.text(key);
    }

    private void refreshResponsiveLayout() {
        if (resizeRefreshQueued) {
            return;
        }

        resizeRefreshQueued = true;
        SwingUtilities.invokeLater(() -> {
            resizeRefreshQueued = false;

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.validate();
            }

            if (mainSplit != null && mainSplit.getWidth() > 0) {
                int chartWidth = Math.max(260, Math.min(360, (int) (mainSplit.getWidth() * 0.28)));
                int divider = LanguageManager.isArabic()
                        ? Math.max(360, mainSplit.getWidth() - chartWidth)
                        : chartWidth;
                mainSplit.setDividerLocation(divider);
                mainSplit.revalidate();
                mainSplit.repaint();
            }

            if (barChart != null) {
                barChart.revalidate();
                barChart.repaint();
            }
            if (pieChart != null) {
                pieChart.revalidate();
                pieChart.repaint();
            }
            if (reportArea != null) {
                reportArea.revalidate();
                reportArea.repaint();
            }

            revalidate();
            repaint();
        });
    }

    
    private JPanel buildHeader() {
        CardLayout cardLayout = new CardLayout();
        JPanel header = new JPanel(cardLayout);
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel normalHeader = new JPanel(new BorderLayout(18, 0));
        normalHeader.setOpaque(false);

        JPanel normalTitleBox = new JPanel();
        normalTitleBox.setLayout(new BoxLayout(normalTitleBox, BoxLayout.Y_AXIS));
        normalTitleBox.setOpaque(false);
        normalTitleBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        normalTitleBox.setComponentOrientation(LanguageManager.orientation());

        JLabel normalTitle = new JLabel(tr("reports.title"));
        normalTitle.setFont(new Font("Tahoma", Font.BOLD, 26));
        normalTitle.setForeground(TEXT);
        normalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        normalTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel normalSub = new JLabel(tr("reports.subtitle"));
        normalSub.setFont(new Font("Tahoma", Font.PLAIN, 13));
        normalSub.setForeground(TEXT_DIM);
        normalSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        normalSub.setHorizontalAlignment(SwingConstants.CENTER);

        normalTitleBox.add(normalTitle);
        normalTitleBox.add(Box.createVerticalStrut(4));
        normalTitleBox.add(normalSub);

        JPanel normalReportActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        normalReportActions.setOpaque(false);
        normalReportActions.add(makeIconButton("\u27F3", tr("reports.refresh"), ACCENT, e -> { play("click"); refresh(); showToast(tr("reports.updated"), SUCCESS); }));
        normalReportActions.add(makeIconButton("\u29C9", tr("reports.copy"), ACTION_SECONDARY, e -> { play("click"); copyReport(); }));
        normalReportActions.add(makeIconButton("\u2913", tr("reports.exportReport"), SUCCESS, e -> { play("click"); exportReport(); }));

        JPanel normalDataActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        normalDataActions.setOpaque(false);
        normalDataActions.add(makeIconButton("\uD83D\uDCBE", tr("reports.saveData"), SUCCESS, e -> { play("click"); onSaveLibraryData(); }));
        normalDataActions.add(makeIconButton("\uD83D\uDCC2", tr("reports.importData"), ACCENT, e -> { play("click"); onLoadLibraryData(); }));

        JPanel normalLeftSlot = new JPanel(new BorderLayout());
        normalLeftSlot.setOpaque(false);
        normalLeftSlot.add(normalDataActions, BorderLayout.WEST);

        JPanel normalRightSlot = new JPanel(new BorderLayout());
        normalRightSlot.setOpaque(false);
        normalRightSlot.add(normalReportActions, BorderLayout.EAST);

        normalHeader.add(normalLeftSlot, BorderLayout.WEST);
        normalHeader.add(normalTitleBox, BorderLayout.CENTER);
        normalHeader.add(normalRightSlot, BorderLayout.EAST);

        JPanel compactHeader = new JPanel(new BorderLayout(0, 10));
        compactHeader.setOpaque(false);

        JPanel compactTitleBox = new JPanel();
        compactTitleBox.setLayout(new BoxLayout(compactTitleBox, BoxLayout.Y_AXIS));
        compactTitleBox.setOpaque(false);
        compactTitleBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        compactTitleBox.setComponentOrientation(LanguageManager.orientation());

        JLabel compactTitle = new JLabel(tr("reports.title"));
        compactTitle.setFont(new Font("Tahoma", Font.BOLD, 26));
        compactTitle.setForeground(TEXT);
        compactTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        compactTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel compactSub = new JLabel(tr("reports.subtitle"));
        compactSub.setFont(new Font("Tahoma", Font.PLAIN, 13));
        compactSub.setForeground(TEXT_DIM);
        compactSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        compactSub.setHorizontalAlignment(SwingConstants.CENTER);

        compactTitleBox.add(compactTitle);
        compactTitleBox.add(Box.createVerticalStrut(4));
        compactTitleBox.add(compactSub);

        JPanel compactReportActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        compactReportActions.setOpaque(false);
        compactReportActions.add(makeIconButton("\u27F3", tr("reports.refresh"), ACCENT, e -> { play("click"); refresh(); showToast(tr("reports.updated"), SUCCESS); }));
        compactReportActions.add(makeIconButton("\u29C9", tr("reports.copy"), ACTION_SECONDARY, e -> { play("click"); copyReport(); }));
        compactReportActions.add(makeIconButton("\u2913", tr("reports.exportReport"), SUCCESS, e -> { play("click"); exportReport(); }));

        JPanel compactDataActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        compactDataActions.setOpaque(false);
        compactDataActions.add(makeIconButton("\uD83D\uDCBE", tr("reports.saveData"), SUCCESS, e -> { play("click"); onSaveLibraryData(); }));
        compactDataActions.add(makeIconButton("\uD83D\uDCC2", tr("reports.importData"), ACCENT, e -> { play("click"); onLoadLibraryData(); }));

        JPanel compactActions = new JPanel(new GridLayout(2, 1, 0, 8));
        compactActions.setOpaque(false);
        compactActions.add(compactReportActions);
        compactActions.add(compactDataActions);

        compactHeader.add(compactTitleBox, BorderLayout.NORTH);
        compactHeader.add(compactActions, BorderLayout.CENTER);

        header.add(normalHeader, "normal");
        header.add(compactHeader, "compact");

        Runnable updateMode = () -> cardLayout.show(header, header.getWidth() > 0 && header.getWidth() < 900 ? "compact" : "normal");
        header.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMode.run();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                updateMode.run();
            }
        });

        SwingUtilities.invokeLater(updateMode);
        return header;
    }
        private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        body.add(buildStatsRow(), BorderLayout.NORTH);

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildChartsPanel(), buildReportPanel());
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(8);
        mainSplit.setResizeWeight(LanguageManager.isArabic() ? 0.72 : 0.28);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOpaque(false);
        mainSplit.setBackground(BG);

        body.add(mainSplit, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        cardBooks     = new StatCard("\uD83D\uDCDA", tr("reports.books"),     ACCENT);
        cardBorrowers = new StatCard("\uD83D\uDC65", tr("reports.borrowers"), PURPLE);
        cardRecords   = new StatCard("\u27F3",       tr("reports.records"),   SUCCESS);
        cardOverdue   = new StatCard("\u23F0",       tr("reports.overdue"),   DANGER);

        row.add(cardBooks);
        row.add(cardBorrowers);
        row.add(cardRecords);
        row.add(cardOverdue);
        return row;
    }
    private JPanel buildChartsPanel() {
        JPanel wrap = new JPanel(new GridLayout(2, 1, 0, 14));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 0, 8));

        barChart = new BarChartPanel();
        pieChart = new PieChartPanel();

        wrap.add(wrapCard(tr("reports.borrowStatus"), barChart));
        wrap.add(wrapCard(tr("reports.copyDistribution"), pieChart));
        return wrap;
    }

    private JPanel buildReportPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(true);
        card.setBackground(CARD);
        card.setBorder(new CompoundRoundedBorder(14, BORDER, new EmptyBorder(14, 14, 14, 14)));

        JLabel hdr = new JLabel(tr("reports.fullReport"));
        hdr.setFont(new Font("Tahoma", Font.BOLD, 15));
        hdr.setForeground(TEXT);
        hdr.setHorizontalAlignment(SwingConstants.RIGHT);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setBackground(new Color(22, 22, 28));
        reportArea.setForeground(TEXT);
        reportArea.setCaretColor(TEXT);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        reportArea.setMargin(new Insets(16, 18, 16, 18));
        reportArea.setLineWrap(false);
        reportArea.setTabSize(4);
        reportArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        JScrollPane sp = new JScrollPane(reportArea);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(new Color(22, 22, 28));
        sp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        sp.getViewport().setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        card.add(hdr, BorderLayout.NORTH);
        card.add(sp,  BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapCard(String title, JComponent inner) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setBackground(CARD);
        card.setBorder(new CompoundRoundedBorder(14, BORDER, new EmptyBorder(12, 14, 12, 14)));

        JLabel l = new JLabel(title);
        l.setForeground(TEXT);
        l.setFont(new Font("Tahoma", Font.BOLD, 14));
        l.setHorizontalAlignment(SwingConstants.RIGHT);

        inner.setOpaque(false);
        card.add(l, BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    
    public void refresh() {
        ArrayList<Book> books = collectBooks();
        ArrayList<Author> authors = collectAuthors();
        ArrayList<BorrowRecord> records = collectRecords();
        if (records  == null) records  = new ArrayList<>();

        int totalBooks   = books.size();
        int borrowers    = countBorrowers();
        int totalRecords = records.size();
        int overdue      = reports.getOverdueRecords(records).size();

        cardBooks.setValueAnimated(totalBooks);
        cardBorrowers.setValueAnimated(borrowers);
        cardRecords.setValueAnimated(totalRecords);
        cardOverdue.setValueAnimated(overdue);

        int active     = reports.getActiveBorrowRecordsCount(records);
        int returned   = reports.getReturnedBorrowRecordsCount(records);
        int activeOnly = Math.max(0, active - overdue);

        
        
        barChart.setData(new int[]{ activeOnly, overdue, returned },
                new String[]{ tr("reports.chart.active"), tr("reports.chart.overdue"), tr("reports.chart.returned") },
                new Color[]{ SUCCESS, DANGER, WARNING });

        int available = reports.getTotalAvailableCopies(books);
        int borrowed  = reports.getBorrowedCopiesCount(books);
        pieChart.setData(new int[]{ available, borrowed },
                new String[]{ tr("reports.chart.available"), tr("reports.chart.borrowed") },
                new Color[]{ WARNING, SUCCESS });

        String full = reports.generateFullReport(books, authors, records);
        reportArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        reportArea.setText(full);
        reportArea.setCaretPosition(0);
    }

    private int countBorrowers() {
        Object list = getList(library, new String[]{"getBorrowers", "getAllBorrowers", "getBorrowerList"});
        if (list instanceof java.util.Collection) return ((java.util.Collection<?>) list).size();
        Object n = invoke(library, "getBorrowersCount");
        if (n instanceof Integer) return (Integer) n;
        return 0;
    }
    private ArrayList<Book> collectBooks() {
        ArrayList<Book> books = new ArrayList<>();
        Object value = getList(library, new String[]{"getBooks", "getAllBooks"});

        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) {
                if (item instanceof Book) {
                    books.add((Book) item);
                }
            }
        }

        return books;
    }

    private ArrayList<Author> collectAuthors() {
        ArrayList<Author> authors = new ArrayList<>();
        Object value = getList(library, new String[]{"getAuthors", "getAllAuthors"});

        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) {
                if (item instanceof Author) {
                    authors.add((Author) item);
                }
            }
        }

        return authors;
    }
    private ArrayList<BorrowRecord> collectRecords() {
        ArrayList<BorrowRecord> records = new ArrayList<>();
        addBorrowRecords(records, getList(library, new String[]{"getAllBorrowRecords", "getBorrowRecords", "getRecords"}));

        Object bm = invoke(library, "getBorrowingManager");
        if (bm != null) {
            addBorrowRecords(records, getList(bm, new String[]{"getAllBorrowRecords", "getBorrowRecords", "getRecords", "getAllRecords"}));
        }

        return records;
    }

    private void addBorrowRecords(ArrayList<BorrowRecord> target, Object value) {
        if (!(value instanceof Iterable<?>)) {
            return;
        }

        for (Object item : (Iterable<?>) value) {
            if (item instanceof BorrowRecord) {
                target.add((BorrowRecord) item);
            }
        }
    }

    
    private void onSaveLibraryData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("library_data.txt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            play("back");
            return;
        }

        boolean saved = dataPersistence.saveToFile(library, chooser.getSelectedFile().getAbsolutePath());

        if (saved) {
            play("success");
            showToast(tr("reports.save.success"), SUCCESS);
        } else {
            play("error");
            showToast(tr("reports.save.fail"), DANGER);
        }
    }

    private void onLoadLibraryData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("library_data.txt"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            play("back");
            return;
        }

        play("warning");
        int confirm = JOptionPane.showConfirmDialog(
                this,
                tr("reports.import.confirm"),
                tr("reports.import.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            play("back");
            return;
        }

        boolean loaded = dataPersistence.loadFromFile(library, chooser.getSelectedFile().getAbsolutePath());

        if (loaded) {
            play("success");
            refresh();
            showToast(tr("reports.import.success"), SUCCESS);
        } else {
            play("error");
            showToast(tr("reports.import.fail"), DANGER);
        }
    }

    private void exportReport() {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File("library_report.txt"));
        int res = ch.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        ArrayList<Book> books = collectBooks();
        ArrayList<Author> authors = collectAuthors();
        ArrayList<BorrowRecord> records = collectRecords();

        boolean ok = reports.exportFullReportToFile(books, authors, records, ch.getSelectedFile().getAbsolutePath());
        if (ok) { play("success"); showToast(tr("reports.export.success"), SUCCESS); }
        else    { play("error");   showToast(tr("reports.export.fail"), DANGER); }
    }

    private void copyReport() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(reportArea.getText()), null);
            play("success");
            showToast(tr("reports.copy.success"), ACCENT);
        } catch (Exception ex) {
            play("error");
            showToast(tr("reports.copy.fail"), DANGER);
        }
    }
    
    private void showToast(String msg, Color color) {
        toast.setText(msg);
        toast.setBackground(color.darker());
        JLayeredPane lp = SwingUtilities.getRootPane(this) != null
                ? SwingUtilities.getRootPane(this).getLayeredPane() : null;
        if (lp == null) return;

        if (toast.getParent() != lp) lp.add(toast, JLayeredPane.POPUP_LAYER);
        Dimension d = toast.getPreferredSize();
        int x = (lp.getWidth() - d.width) / 2;
        toast.setBounds(x, -d.height, d.width, d.height);
        toast.setVisible(true);

        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        final int targetY = 18;
        final long start = System.currentTimeMillis();
        toastTimer = new Timer(15, null);
        toastTimer.addActionListener(new ActionListener() {
            int phase = 0; long t0 = start;
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                float p = Math.min(1f, (now - t0) / 280f);
                if (phase == 0) {
                    int y = (int) (-d.height + (targetY + d.height) * easeOut(p));
                    toast.setLocation(toast.getX(), y);
                    if (p >= 1f) { phase = 1; t0 = now + 1400; }
                } else if (phase == 1) {
                    if (now >= t0) { phase = 2; t0 = now; }
                } else {
                    float q = Math.min(1f, (now - t0) / 240f);
                    int y = (int) (targetY - (targetY + d.height) * easeIn(q));
                    toast.setLocation(toast.getX(), y);
                    if (q >= 1f) { toast.setVisible(false); toastTimer.stop(); }
                }
            }
        });
        toastTimer.start();
    }
    private float easeOut(float t){ return 1f - (float)Math.pow(1-t, 3); }
    private float easeIn (float t){ return (float)Math.pow(t, 3); }

    
    private static Object invoke(Object target, String name, Object... args) {
        if (target == null) return null;
        try {
            for (Method m : target.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                    m.setAccessible(true);
                    return m.invoke(target, args);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
    private static Object getList(Object target, String[] names) {
        for (String n : names) {
            Object r = invoke(target, n);
            if (r != null) return r;
        }
        return null;
    }
    private static void play(String k) {
        try { SoundManager.play(k); } catch (Throwable ignored) {}
    }

    
    private JButton makeIconButton(String iconChar, String text, Color color, ActionListener al) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = color;
                if (getModel().isPressed())      c = color.darker();
                else if (getModel().isRollover()) c = color.brighter();
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JLabel ic = new JLabel(iconChar);
        ic.setFont(new Font("Segoe UI Symbol", Font.BOLD, 15));
        ic.setForeground(Color.WHITE);
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Tahoma", Font.BOLD, 13));
        lb.setForeground(Color.WHITE);
        b.add(ic); b.add(lb);

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.addActionListener(al);
        return b;
    }

    
    static class StatCard extends JPanel {
        private final JLabel iconL  = new JLabel();
        private final JLabel titleL = new JLabel();
        private final JLabel valueL = new JLabel("0");
        private final Color accent;
        private int currentValue = 0;
        private Timer animTimer;

        StatCard(String icon, String title, Color accent) {
            this.accent = accent;
            setOpaque(false);
            setBorder(new CompoundRoundedBorder(16, BORDER, new EmptyBorder(16, 18, 16, 18)));
            setLayout(new BorderLayout(10, 0));

            iconL.setText(icon);
            iconL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            iconL.setForeground(accent);
            iconL.setHorizontalAlignment(SwingConstants.CENTER);
            iconL.setPreferredSize(new Dimension(54, 54));
            iconL.setOpaque(true);
            iconL.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            titleL.setText(title);
            titleL.setFont(new Font("Tahoma", Font.PLAIN, 13));
            titleL.setForeground(TEXT_DIM);
            titleL.setAlignmentX(Component.RIGHT_ALIGNMENT);
            valueL.setFont(new Font("Tahoma", Font.BOLD, 26));
            valueL.setForeground(TEXT);
            valueL.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(titleL);
            right.add(Box.createVerticalStrut(4));
            right.add(valueL);

            add(iconL, BorderLayout.WEST);
            add(right, BorderLayout.CENTER);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            g2.dispose();
            super.paintComponent(g);
        }

        void setValueAnimated(int target) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            final int from = currentValue;
            final long start = System.currentTimeMillis();
            final int dur = 600;
            animTimer = new Timer(15, e -> {
                float p = Math.min(1f, (System.currentTimeMillis() - start) / (float) dur);
                int v = (int) (from + (target - from) * (1 - Math.pow(1 - p, 3)));
                valueL.setText(String.valueOf(v));
                if (p >= 1f) { valueL.setText(String.valueOf(target)); currentValue = target; ((Timer)e.getSource()).stop(); }
            });
            animTimer.start();
        }
    }

    
    static class BarChartPanel extends JPanel {
        private int[] values = new int[0];
        private String[] labels = new String[0];
        private Color[] colors = new Color[0];
        private float progress = 0f;
        private Timer t;

        BarChartPanel() { setPreferredSize(new Dimension(100, 180)); }

        void setData(int[] v, String[] l, Color[] c) {
            this.values = v; this.labels = l; this.colors = c;
            progress = 0f;
            if (t != null && t.isRunning()) t.stop();
            long start = System.currentTimeMillis();
            t = new Timer(15, e -> {
                progress = Math.min(1f, (System.currentTimeMillis() - start) / 700f);
                repaint();
                if (progress >= 1f) ((Timer)e.getSource()).stop();
            });
            t.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int pad = 26, bottom = 58, top = 14;
            int chartH = Math.max(20, h - bottom - top);
            int max = 1; for (int v : values) max = Math.max(max, v);
            int n = values.length;
            int slotW = Math.max(1, (w - pad * 2) / n);
            int bw = Math.max(18, Math.min(46, slotW - 28));

            g2.setColor(BORDER);
            g2.drawLine(pad, h - bottom, w - pad, h - bottom);

            for (int i = 0; i < n; i++) {
                int slotX = pad + i * slotW;
                int x = slotX + (slotW - bw) / 2;
                int barH = (int) ((values[i] / (float) max) * chartH * progress);
                int y = h - bottom - barH;
                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y, bw, barH, 8, 8);

                g2.setColor(TEXT);
                g2.setFont(new Font("Tahoma", Font.BOLD, 12));
                String vs = String.valueOf(values[i]);
                int sw = g2.getFontMetrics().stringWidth(vs);
                g2.drawString(vs, x + (bw - sw) / 2, y - 5);
            }

            drawLegend(g2, w, h);
            g2.dispose();
        }

        private void drawLegend(Graphics2D g2, int w, int h) {
            g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int gap = 10;
            int itemGap = 14;
            int square = 10;
            int x = 26;
            int y = h - 34;
            int lineHeight = 18;

            for (int i = 0; i < labels.length; i++) {
                String label = labels[i] == null ? "" : labels[i];
                int itemW = square + 5 + fm.stringWidth(label);
                if (x > 26 && x + itemW > w - 22) {
                    x = 26;
                    y += lineHeight;
                }
                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y - 9, square, square, 4, 4);
                g2.setColor(TEXT_DIM);
                g2.drawString(label, x + square + 5, y);
                x += itemW + itemGap + gap;
            }
        }
    }

    
    static class PieChartPanel extends JPanel {
        private int[] values = new int[0];
        private String[] labels = new String[0];
        private Color[] colors = new Color[0];
        private float progress = 0f;
        private Timer t;

        PieChartPanel() { setPreferredSize(new Dimension(100, 180)); }

        void setData(int[] v, String[] l, Color[] c) {
            this.values = v; this.labels = l; this.colors = c;
            progress = 0f;
            if (t != null && t.isRunning()) t.stop();
            long start = System.currentTimeMillis();
            t = new Timer(15, e -> {
                progress = Math.min(1f, (System.currentTimeMillis() - start) / 800f);
                repaint();
                if (progress >= 1f) ((Timer)e.getSource()).stop();
            });
            t.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int total = 0; for (int v : values) total += v;
            if (total == 0) {
                g2.setColor(TEXT_DIM);
                g2.setFont(new Font("Tahoma", Font.PLAIN, 13));
                String s = ReportsPanel.tr("reports.noData");
                int sw = g2.getFontMetrics().stringWidth(s);
                g2.drawString(s, (w - sw) / 2, h / 2);
                g2.dispose(); return;
            }

            int legendArea = 48;
            int size = Math.max(76, Math.min(w - 44, h - legendArea - 22));
            int x = (w - size) / 2;
            int y = 14;

            float start = 90f;
            for (int i = 0; i < values.length; i++) {
                float angle = (values[i] / (float) total) * 360f * progress;
                g2.setColor(colors[i]);
                g2.fillArc(x, y, size, size, (int) start, (int) -angle);
                start -= angle;
            }
            g2.setColor(CARD);
            int hole = Math.max(42, size - 70);
            g2.fillOval(x + (size - hole) / 2, y + (size - hole) / 2, hole, hole);

            g2.setColor(TEXT);
            g2.setFont(new Font("Tahoma", Font.BOLD, 20));
            String ts = String.valueOf(total);
            int sw = g2.getFontMetrics().stringWidth(ts);
            g2.drawString(ts, x + (size - sw) / 2, y + size / 2 + 4);
            g2.setColor(TEXT_DIM);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
            String totalLabel = ReportsPanel.tr("reports.totalCopies");
            int tw = g2.getFontMetrics().stringWidth(totalLabel);
            g2.drawString(totalLabel, x + (size - tw) / 2, y + size / 2 + 22);

            drawLegend(g2, w, h);
            g2.dispose();
        }

        private void drawLegend(Graphics2D g2, int w, int h) {
            g2.setFont(new Font("Tahoma", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int square = 11;
            int itemGap = 16;
            int x = 22;
            int y = h - 32;
            int lineHeight = 18;

            for (int i = 0; i < labels.length; i++) {
                String label = (labels[i] == null ? "" : labels[i]) + " : " + values[i];
                int itemW = square + 6 + fm.stringWidth(label);
                if (x > 22 && x + itemW > w - 16) {
                    x = 22;
                    y += lineHeight;
                }
                g2.setColor(colors[i]);
                g2.fillRoundRect(x, y - 9, square, square, 4, 4);
                g2.setColor(TEXT);
                g2.drawString(label, x + square + 6, y);
                x += itemW + itemGap;
            }
        }
    }
    
    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= getHgap() + 1;
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0 && target.getParent() != null) {
                    targetWidth = target.getParent().getWidth();
                }
                if (targetWidth == 0) {
                    targetWidth = 620;
                }

                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component component = target.getComponent(i);
                    if (!component.isVisible()) {
                        continue;
                    }

                    Dimension d = preferred ? component.getPreferredSize() : component.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth != 0) {
                        rowWidth += getHgap();
                    }
                    rowWidth += d.width;
                    rowHeight = Math.max(rowHeight, d.height);
                }

                addRow(dim, rowWidth, rowHeight);
                dim.width += insets.left + insets.right + getHgap() * 2;
                dim.height += insets.top + insets.bottom + getVgap() * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }
    
    static class CompoundRoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final EmptyBorder inner;
        CompoundRoundedBorder(int r, Color c, EmptyBorder i) { radius = r; color = c; inner = i; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return inner.getBorderInsets(c); }
        @Override public Insets getBorderInsets(Component c, Insets insets) { return inner.getBorderInsets(c, insets); }
    }
}



