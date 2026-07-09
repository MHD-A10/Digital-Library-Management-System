package gui;

import data.*;
import model.*;
import service.*;
import trees.*;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BorrowerFormDialog extends JDialog {
    private boolean saved = false;
    private JTextField idField;
    private JTextField nameField;
    private JRadioButton studentRadio;
    private JRadioButton graduateRadio;

    private static final Color BG = new Color(18, 18, 22);
    private static final Color FG = new Color(230, 230, 235);
    private static final Color MUTED = new Color(160, 160, 170);
    private static final Color ACCENT = new Color(79, 140, 255);
    private static final Color FIELD = new Color(28, 28, 33);
    private static final String EMOJI_FONT = "Segoe UI Emoji";
    private static final String TEXT_FONT = "Tahoma";

    public BorrowerFormDialog(Window owner) {
        super(owner, LanguageManager.text("borrowerForm.title"), ModalityType.APPLICATION_MODAL);
        applyComponentOrientation(LanguageManager.orientation());
        setSize(460, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.applyComponentOrientation(LanguageManager.orientation());

        JPanel titleWrap = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        titleWrap.setBackground(BG);
        JLabel icon = new JLabel("\uD83D\uDC64");
        icon.setFont(new Font(EMOJI_FONT, Font.PLAIN, 20));
        icon.setForeground(FG);
        JLabel title = new JLabel(LanguageManager.text("borrowerForm.heading"));
        title.setFont(new Font(TEXT_FONT, Font.BOLD, 18));
        title.setForeground(FG);
        titleWrap.add(icon);
        titleWrap.add(title);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(14, 0, 10, 0));
        form.applyComponentOrientation(LanguageManager.orientation());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 6, 7, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1;

        idField = makeField(LanguageManager.text("borrowerForm.id.placeholder"));
        nameField = makeField(LanguageManager.text("borrowerForm.name.placeholder"));

        c.gridx = 0; c.gridy = 0; c.weightx = 0; form.add(label(LanguageManager.text("borrowerForm.label.id")), c);
        c.gridx = 1; c.weightx = 1; form.add(idField, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; form.add(label(LanguageManager.text("borrowerForm.label.name")), c);
        c.gridx = 1; c.weightx = 1; form.add(nameField, c);

        studentRadio = makeRadio(LanguageManager.text("borrowerForm.student"), true);
        graduateRadio = makeRadio(LanguageManager.text("borrowerForm.graduate"), false);
        ButtonGroup group = new ButtonGroup();
        group.add(studentRadio);
        group.add(graduateRadio);

        JPanel typePanel = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 14, 0));
        typePanel.setBackground(BG);
        typePanel.applyComponentOrientation(LanguageManager.orientation());
        typePanel.add(studentRadio);
        typePanel.add(graduateRadio);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; form.add(label(LanguageManager.text("borrowerForm.label.type")), c);
        c.gridx = 1; c.weightx = 1; form.add(typePanel, c);

        JLabel hint = new JLabel(LanguageManager.text("borrowerForm.hint"));
        hint.setForeground(MUTED);
        hint.setFont(new Font(TEXT_FONT, Font.PLAIN, 11));
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        form.add(hint, c);

        JPanel actions = new JPanel(new FlowLayout(LanguageManager.isArabic() ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        actions.setBackground(BG);
        JButton save = pillButton(LanguageManager.text("borrowerForm.save"), ACCENT, Color.WHITE);
        JButton cancel = pillButton(LanguageManager.text("borrowerForm.cancel"), FIELD, FG);
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> { SoundManager.play("back"); dispose(); });
        actions.add(save);
        actions.add(cancel);

        root.add(titleWrap, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(save);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font(TEXT_FONT, Font.BOLD, 12));
        l.setHorizontalAlignment(LanguageManager.isArabic() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        return l;
    }

    private JTextField makeField(String hint) {
        JTextField f = new JTextField();
        f.setBackground(FIELD);
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1, true), new EmptyBorder(7, 10, 7, 10)));
        f.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, hint);
        f.setComponentOrientation(LanguageManager.orientation());
        return f;
    }

    private JRadioButton makeRadio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setBackground(BG);
        r.setForeground(FG);
        r.setFont(new Font(TEXT_FONT, Font.PLAIN, 13));
        r.setFocusPainted(false);
        r.applyComponentOrientation(LanguageManager.orientation());
        r.addActionListener(e -> SoundManager.play("click"));
        return r;
    }

    private JButton pillButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font(TEXT_FONT, Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void onSave() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        if (id.isEmpty()) { showValidation(LanguageManager.text("borrowerForm.idRequired"), idField); return; }
        if (name.isEmpty()) { showValidation(LanguageManager.text("borrowerForm.nameRequired"), nameField); return; }
        SoundManager.play("click");
        saved = true;
        dispose();
    }

    private void showValidation(String message, JTextField field) {
        SoundManager.play("error");
        shake(field);
        JOptionPane.showMessageDialog(this, message, LanguageManager.text("borrowerForm.error"), JOptionPane.ERROR_MESSAGE);
    }

    private void shake(JComponent comp) {
        final Point original = comp.getLocation();
        final Timer timer = new Timer(15, null);
        final int[] step = {0};
        timer.addActionListener(e -> {
            int dx = (step[0] % 2 == 0) ? 6 : -6;
            comp.setLocation(original.x + dx, original.y);
            step[0]++;
            if (step[0] > 8) { comp.setLocation(original); ((Timer) e.getSource()).stop(); }
        });
        timer.start();
    }

    public static class Result {
        public final String id;
        public final String name;
        public final boolean graduate;
        Result(String id, String name, boolean graduate) {
            this.id = id; this.name = name; this.graduate = graduate;
        }
    }

    public Result showDialog() {
        setVisible(true);
        if (!saved) return null;
        return new Result(idField.getText().trim(), nameField.getText().trim(), graduateRadio.isSelected());
    }
}