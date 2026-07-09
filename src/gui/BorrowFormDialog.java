package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class BorrowFormDialog extends JDialog {
    private LibraryManager library;
    private JTextField borrowerField;
    private JTextField isbnField;
    private JLabel borrowerIcon, borrowerText;
    private JLabel bookIcon, bookText;
    private boolean confirmed = false;

    private static final Color BG = new Color(18, 18, 22);
    private static final Color SURFACE = new Color(28, 28, 33);
    private static final Color ACCENT = new Color(79, 140, 255);
    private static final Color TEXT = new Color(240, 240, 245);
    private static final Color TEXT_DIM = new Color(160, 160, 170);
    private static final Color ERROR = new Color(255, 90, 90);
    private static final Color SUCCESS_C = new Color(80, 220, 120);

    public BorrowFormDialog(JFrame parent, LibraryManager library) {
        super(parent, LanguageManager.text("borrowForm.title"), true);
        this.library = library;
        setSize(480, 400);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 480, 400, 20, 20));
        applyComponentOrientation(LanguageManager.orientation());
        initUI();
        animateEntry();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));
        main.applyComponentOrientation(LanguageManager.orientation());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        header.setBackground(BG);
        JLabel iconLbl = new JLabel("\uD83D\uDCDA");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel titleLbl = new JLabel(LanguageManager.text("borrowForm.title"));
        titleLbl.setFont(new Font("Tahoma", Font.BOLD, 18));
        titleLbl.setForeground(TEXT);
        header.add(iconLbl);
        header.add(titleLbl);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setComponentOrientation(LanguageManager.orientation());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        JLabel lblBorrower = fieldLabel(LanguageManager.text("borrowForm.borrowerLabel"));
        borrowerField = createField();
        borrowerIcon = new JLabel(" ");
        borrowerText = new JLabel(" ");
        JPanel borrowerPrev = buildPreviewRow(borrowerIcon, borrowerText);

        JLabel lblIsbn = fieldLabel(LanguageManager.text("borrowForm.isbnLabel"));
        isbnField = createField();
        bookIcon = new JLabel(" ");
        bookText = new JLabel(" ");
        JPanel bookPrev = buildPreviewRow(bookIcon, bookText);

        gbc.gridy = 0; form.add(lblBorrower, gbc);
        gbc.gridy = 1; form.add(borrowerField, gbc);
        gbc.gridy = 2; form.add(borrowerPrev, gbc);
        gbc.gridy = 3; form.add(lblIsbn, gbc);
        gbc.gridy = 4; form.add(isbnField, gbc);
        gbc.gridy = 5; form.add(bookPrev, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.setBackground(BG);
        JButton saveBtn = createButton(LanguageManager.text("borrowForm.save"), ACCENT);
        JButton cancelBtn = createButton(LanguageManager.text("borrowForm.cancel"), new Color(80, 80, 90));
        saveBtn.addActionListener(e -> doSave());
        getRootPane().setDefaultButton(saveBtn);
        cancelBtn.addActionListener(e -> {
            try { SoundManager.play("click"); } catch (Exception ex) { Toolkit.getDefaultToolkit().beep(); }
            animateExit();
        });
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        borrowerField.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { updateBorrowerPreview(); } });
        isbnField.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { updateBookPreview(); } });

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Tahoma", Font.BOLD, 13));
        label.setForeground(TEXT);
        label.setHorizontalAlignment(LanguageManager.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        return label;
    }

    private JPanel buildPreviewRow(JLabel icon, JLabel text) {
        JPanel p = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setComponentOrientation(LanguageManager.orientation());
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        icon.setForeground(TEXT_DIM);
        text.setFont(new Font("Tahoma", Font.PLAIN, 12));
        text.setForeground(TEXT_DIM);
        p.add(text);
        p.add(icon);
        return p;
    }

    private JTextField createField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Tahoma", Font.PLAIN, 13));
        field.setBackground(SURFACE);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 55), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(300, 36));
        field.setComponentOrientation(LanguageManager.orientation());
        return field;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Tahoma", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void setPreview(JLabel icon, JLabel text, String iconStr, String msg, Color color) {
        icon.setText(iconStr);
        icon.setForeground(color);
        text.setText(msg);
        text.setForeground(color);
    }

    private void updateBorrowerPreview() {
        String id = borrowerField.getText().trim();
        if (id.isEmpty()) { borrowerIcon.setText(" "); borrowerText.setText(" "); return; }
        Borrower borrower = library.searchBorrowerById(id);
        if (borrower != null) {
            setPreview(borrowerIcon, borrowerText, "\u2713",
                    borrower.getName() + " | " + LanguageManager.text("borrowForm.preview.active") + ": " + borrower.getActiveBorrowCount()
                            + (borrower.isGraduatingStudent() ? " | " + LanguageManager.text("borrowForm.preview.graduate") : ""),
                    SUCCESS_C);
        } else {
            setPreview(borrowerIcon, borrowerText, "\u26A0", LanguageManager.text("borrowForm.preview.borrowerMissing"), ERROR);
        }
    }

    private void updateBookPreview() {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) { bookIcon.setText(" "); bookText.setText(" "); return; }
        Book book = library.searchBookByISBN(isbn);
        if (book != null) {
            int available = book.getAvailableCopies();
            setPreview(bookIcon, bookText, available > 0 ? "\u2713" : "\u26A0",
                    book.getTitle() + " | " + LanguageManager.text("borrowForm.preview.available") + ": " + available + "/" + book.getTotalCopies(),
                    available > 0 ? SUCCESS_C : ERROR);
        } else {
            setPreview(bookIcon, bookText, "\u26A0", LanguageManager.text("borrowForm.preview.bookMissing"), ERROR);
        }
    }

    private void doSave() {
        String borrowerId = borrowerField.getText().trim();
        String isbn = isbnField.getText().trim();
        if (borrowerId.isEmpty() || isbn.isEmpty()) { shakeField(borrowerId.isEmpty() ? borrowerField : isbnField); playError(); return; }
        BorrowResult result = library.borrowBook(borrowerId, isbn);
        if (result == BorrowResult.SUCCESS) {
            confirmed = true; playSuccess();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.success"), LanguageManager.text("borrowForm.success.title"), JOptionPane.INFORMATION_MESSAGE);
            animateExit();
        } else if (result == BorrowResult.WAITLISTED) {
            confirmed = true; playSuccess();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.waitlisted"), LanguageManager.text("borrowForm.waitlist.title"), JOptionPane.INFORMATION_MESSAGE);
            animateExit();
        } else if (result == BorrowResult.ALREADY_WAITLISTED) {
            shakeField(isbnField); playError();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.alreadyWaitlisted"), LanguageManager.text("borrowForm.warning"), JOptionPane.WARNING_MESSAGE);
        } else if (result == BorrowResult.ALREADY_BORROWED) {
            shakeField(isbnField); playError();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.alreadyBorrowed"), LanguageManager.text("borrowForm.warning"), JOptionPane.WARNING_MESSAGE);
        } else if (result == BorrowResult.BORROWER_LIMIT_REACHED) {
            shakeField(borrowerField); playError();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.limitReached"), LanguageManager.text("borrowForm.warning"), JOptionPane.WARNING_MESSAGE);
        } else if (result == BorrowResult.NOT_AVAILABLE) {
            shakeField(isbnField); playError();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.notAvailable"), LanguageManager.text("borrowForm.warning"), JOptionPane.WARNING_MESSAGE);
        } else {
            shakeField(borrowerField); shakeField(isbnField); playError();
            JOptionPane.showMessageDialog(this, LanguageManager.text("borrowForm.invalid"), LanguageManager.text("borrowForm.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() { return confirmed; }

    private void animateEntry() {
        setOpacity(0f);
        Timer timer = new Timer(16, null);
        final float[] alpha = {0f};
        timer.addActionListener(e -> { alpha[0] += 0.08f; if (alpha[0] >= 1f) { alpha[0] = 1f; timer.stop(); } setOpacity(alpha[0]); });
        timer.start();
    }

    private void animateExit() {
        Timer timer = new Timer(16, null);
        final float[] alpha = {1f};
        timer.addActionListener(e -> { alpha[0] -= 0.08f; if (alpha[0] <= 0f) { alpha[0] = 0f; timer.stop(); dispose(); } setOpacity(alpha[0]); });
        timer.start();
    }

    private void shakeField(JComponent comp) {
        Point orig = comp.getLocation();
        final int[] count = {0};
        Timer timer = new Timer(20, null);
        timer.addActionListener(e -> { count[0]++; comp.setLocation(orig.x + (count[0] % 2 == 0 ? 6 : -6), orig.y); if (count[0] >= 10) { comp.setLocation(orig); timer.stop(); } });
        timer.start();
    }

    private void playSuccess() { try { SoundManager.play("success"); } catch (Exception e) { Toolkit.getDefaultToolkit().beep(); } }
    private void playError() { try { SoundManager.play("error"); } catch (Exception e) { Toolkit.getDefaultToolkit().beep(); } }
}