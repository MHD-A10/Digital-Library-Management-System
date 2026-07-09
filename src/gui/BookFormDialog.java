package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BookFormDialog extends JDialog {
    private boolean saved = false;

    private JTextField isbnField;
    private JTextField titleField;
    private JTextField authorField;
    private JSpinner copiesSpinner;

    private static final Color BG = new Color(18, 18, 22);
    private static final Color FG = new Color(230, 230, 235);
    private static final Color MUTED = new Color(160, 160, 170);
    private static final Color ACCENT = new Color(79, 140, 255);
    private static final Color FIELD = new Color(28, 28, 33);
    private static final Color BORDER = new Color(58, 58, 68);
    private static final String EMOJI_FONT = "Segoe UI Emoji";
    private static final String TEXT_FONT = "Tahoma";

    public BookFormDialog(Window owner) {
        super(owner, LanguageManager.text("bookForm.title"), ModalityType.APPLICATION_MODAL);
        applyComponentOrientation(LanguageManager.orientation());
        setSize(460, 390);
        setMinimumSize(new Dimension(460, 390));
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.applyComponentOrientation(LanguageManager.orientation());

        JPanel titleWrap = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        titleWrap.setOpaque(false);
        titleWrap.applyComponentOrientation(LanguageManager.orientation());

        JLabel icon = new JLabel("\uD83D\uDCD6");
        icon.setFont(new Font(EMOJI_FONT, Font.PLAIN, 20));
        icon.setForeground(FG);

        JLabel title = new JLabel(LanguageManager.text("bookForm.title"));
        title.setFont(new Font(TEXT_FONT, Font.BOLD, 18));
        title.setForeground(FG);

        titleWrap.add(icon);
        titleWrap.add(title);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.applyComponentOrientation(LanguageManager.orientation());
        form.setBorder(new EmptyBorder(20, 0, 14, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 6, 7, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1;

        isbnField = makeField(LanguageManager.text("bookForm.isbn.placeholder"));
        titleField = makeField(LanguageManager.text("bookForm.title.placeholder"));
        authorField = makeField(LanguageManager.text("bookForm.author.placeholder"));
        copiesSpinner = makeSpinner();

        addRow(form, c, 0, "ISBN:", isbnField);
        addRow(form, c, 1, LanguageManager.text("bookForm.label.title"), titleField);
        addRow(form, c, 2, LanguageManager.text("bookForm.label.author"), authorField);
        addRow(form, c, 3, LanguageManager.text("bookForm.label.copies"), copiesSpinner);


        JPanel actions = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.applyComponentOrientation(LanguageManager.orientation());

        JButton save = pillButton(LanguageManager.text("bookForm.save"), ACCENT, Color.WHITE);
        JButton cancel = pillButton(LanguageManager.text("bookForm.cancel"), FIELD, FG);

        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> {
            SoundManager.play("back");
            dispose();
        });

        actions.add(save);
        actions.add(cancel);

        root.add(titleWrap, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(save);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String labelText, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(label(labelText), c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font(TEXT_FONT, Font.BOLD, 12));
        l.setOpaque(false);
        l.setHorizontalAlignment(LanguageManager.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        return l;
    }

    private JTextField makeField(String hint) {
        JTextField f = new JTextField();
        f.setBackground(FIELD);
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(7, 10, 7, 10)
        ));
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
        f.setComponentOrientation(LanguageManager.orientation());
        return f;
    }

    private JSpinner makeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinner.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        spinner.setBackground(FIELD);
        spinner.setForeground(FG);
        spinner.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        spinner.setComponentOrientation(LanguageManager.orientation());
        spinner.setPreferredSize(new Dimension(220, 34));

        JComponent editor = spinner.getEditor();
        editor.setOpaque(false);
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(FIELD);
            tf.setForeground(FG);
            tf.setCaretColor(FG);
            tf.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
            tf.setBorder(new EmptyBorder(7, 10, 7, 10));
        }
        return spinner;
    }

    private JButton pillButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font(TEXT_FONT, Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void onSave() {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (isbn.isEmpty()) {
            showValidationMessage(LanguageManager.text("bookForm.isbnRequired"), LanguageManager.text("bookForm.warning"), JOptionPane.WARNING_MESSAGE, isbnField);
            return;
        }

        if (!isbn.matches("\\d{3}-\\d{3}")) {
            showValidationMessage(LanguageManager.text("bookForm.invalidIsbn"), LanguageManager.text("bookForm.invalidIsbnTitle"), JOptionPane.ERROR_MESSAGE, isbnField);
            return;
        }

        if (title.isEmpty()) {
            showValidationMessage(LanguageManager.text("bookForm.titleRequired"), LanguageManager.text("bookForm.warning"), JOptionPane.WARNING_MESSAGE, titleField);
            return;
        }

        if (author.isEmpty()) {
            showValidationMessage(LanguageManager.text("bookForm.authorRequired"), LanguageManager.text("bookForm.warning"), JOptionPane.WARNING_MESSAGE, authorField);
            return;
        }

        int copies;
        try {
            copies = (Integer) copiesSpinner.getValue();
        } catch (NumberFormatException e) {
            showValidationMessage(LanguageManager.text("bookForm.copiesRequired"), LanguageManager.text("bookForm.warning"), JOptionPane.WARNING_MESSAGE, copiesSpinner);
            return;
        }

        if (copies < 1 || copies > 999) {
            showValidationMessage(LanguageManager.text("bookForm.copiesRequired"), LanguageManager.text("bookForm.warning"), JOptionPane.WARNING_MESSAGE, copiesSpinner);
            return;
        }

        saved = true;
        dispose();
    }

    private void showValidationMessage(String message, String title, int type, JComponent field) {
        SoundManager.play("error");
        JOptionPane.showMessageDialog(this, message, title, type);
        shake(field);
        field.requestFocus();
    }

    private void shake(JComponent c) {
        final Point original = c.getLocation();
        final int[] step = {0};
        final int[] offsets = {-6, 6, -5, 5, -3, 3, 0};

        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            if (step[0] >= offsets.length) {
                c.setLocation(original);
                ((Timer) e.getSource()).stop();
                return;
            }
            c.setLocation(original.x + offsets[step[0]], original.y);
            step[0]++;
        });
        t.start();
    }

    public static class Result {
        public final String isbn, title, author;
        public final int copies;

        public Result(String isbn, String title, String author, int copies) {
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.copies = copies;
        }
    }

    public Result showDialog() {
        setVisible(true);
        if (!saved) return null;
        return new Result(
                isbnField.getText().trim(),
                titleField.getText().trim(),
                authorField.getText().trim(),
                (Integer) copiesSpinner.getValue()
        );
    }
}