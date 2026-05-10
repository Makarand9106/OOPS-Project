package gui;

import services.UserService;
import users.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginPanel — the welcome / sign-in screen.
 *
 * GUI ↔ Logic connection:
 *   - Calls userService.login(id, password) to authenticate.
 *   - On success, delegates to MainFrame.showDashboard(user) to navigate.
 *   - Role combobox drives which dashboard opens; mismatched role shows error.
 */
public class LoginPanel extends JPanel {

    // ── Colour palette ────────────────────────────────────────────────────────
    static final Color NAVY      = new Color(0x1A2332);
    static final Color COEP_BLUE = new Color(0x003F7D);
    static final Color GOLD      = new Color(0xF5A623);
    static final Color OFF_WHITE = new Color(0xF4F6FB);
    static final Color LIGHT_GRAY = new Color(0xDDE3ED);
    static final Color ERROR_RED  = new Color(0xCC2200);
    static final Color SUCCESS_GREEN = new Color(0x1A7A3F);

    private final MainFrame   frame;
    private final UserService userService;

    // Form components
    private JTextField   idField;
    private JPasswordField passField;
    private JComboBox<String> roleCombo;
    private JLabel       statusLabel;

    // ─────────────────────────────────────────────────────────────────────────

    public LoginPanel(MainFrame frame, UserService userService) {
        this.frame       = frame;
        this.userService = userService;
        setBackground(OFF_WHITE);
        setLayout(new GridBagLayout());
        buildUI();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        // ── Left decorative panel ─────────────────────────────────────────────
        JPanel leftPanel = buildBrandPanel();

        // ── Right login card ───────────────────────────────────────────────────
        JPanel loginCard = buildLoginCard();

        // Combine left + right in a split panel
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        splitPanel.setPreferredSize(new Dimension(860, 520));
        splitPanel.add(leftPanel);
        splitPanel.add(loginCard);

        add(splitPanel);
    }

    // ── Brand panel (left half) ──────────────────────────────────────────────

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background: navy → COEP blue
                GradientPaint gp = new GradientPaint(0, 0, NAVY, 0, getHeight(), COEP_BLUE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-60, -60, 260, 260);
                g2.fillOval(getWidth() - 130, getHeight() - 130, 200, 200);
            }
        };
        panel.setPreferredSize(new Dimension(380, 520));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Padding
        panel.add(Box.createVerticalStrut(60));

        // Institution icon placeholder
        JLabel iconLabel = new JLabel("🎓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setForeground(GOLD);
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(iconLabel);

        panel.add(Box.createVerticalStrut(24));

        JLabel titleLabel = makeLabel("COEP", new Font("Segoe UI", Font.BOLD, 32), Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(titleLabel);

        JLabel subLabel = makeLabel("College of Engineering Pune",
                new Font("Segoe UI", Font.PLAIN, 14), new Color(0xBBCCDD));
        subLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subLabel);

        panel.add(Box.createVerticalStrut(16));

        JLabel erpLabel = makeLabel("Academic Portal",
                new Font("Segoe UI", Font.BOLD, 18), GOLD);
        erpLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(erpLabel);

        panel.add(Box.createVerticalStrut(40));

        // Tagline bullets
        for (String line : new String[]{
                "📚  Course Management",
                "📝  Assignments & Quizzes",
                "📊  Exam & Results" }) {
            JLabel l = makeLabel(line, new Font("Segoe UI", Font.PLAIN, 13),
                    new Color(0xCCDDEE));
            l.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(l);
            panel.add(Box.createVerticalStrut(8));
        }

        return panel;
    }

    // ── Login card (right half) ──────────────────────────────────────────────

    private JPanel buildLoginCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(7, 30, 7, 30);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;
        gc.gridx   = 0;

        // Title
        gc.gridy   = 0;
        gc.insets  = new Insets(36, 40, 2, 40);
        JLabel heading = makeLabel("Welcome Back", new Font("Segoe UI", Font.BOLD, 26), NAVY);
        card.add(heading, gc);

        gc.gridy   = 1;
        gc.insets  = new Insets(0, 40, 20, 40);
        JLabel sub = makeLabel("Sign in to your account", new Font("Segoe UI", Font.PLAIN, 13),
                new Color(0x778899));
        card.add(sub, gc);

        // Role selector
        gc.gridy   = 2;
        gc.insets  = new Insets(6, 40, 2, 40);
        card.add(makeLabel("Login As", new Font("Segoe UI", Font.BOLD, 12), NAVY), gc);

        gc.gridy   = 3;
        gc.insets  = new Insets(0, 40, 12, 40);
        roleCombo  = new JComboBox<>(new String[]{"STUDENT", "TEACHER", "ADMIN", "EXAM_CELL"});
        styleCombo(roleCombo);
        card.add(roleCombo, gc);

        // User ID
        gc.gridy   = 4;
        gc.insets  = new Insets(6, 40, 2, 40);
        card.add(makeLabel("User ID", new Font("Segoe UI", Font.BOLD, 12), NAVY), gc);

        gc.gridy   = 5;
        gc.insets  = new Insets(0, 40, 12, 40);
        idField    = new JTextField();
        styleTextField(idField, "Enter your numeric ID");
        card.add(idField, gc);

        // Password
        gc.gridy   = 6;
        gc.insets  = new Insets(6, 40, 2, 40);
        card.add(makeLabel("Password", new Font("Segoe UI", Font.BOLD, 12), NAVY), gc);

        gc.gridy   = 7;
        gc.insets  = new Insets(0, 40, 16, 40);
        passField  = new JPasswordField();
        styleTextField(passField, "Enter your password");
        // Allow Enter key to trigger login
        passField.addActionListener(e -> doLogin());
        card.add(passField, gc);

        // Status / error label
        gc.gridy   = 8;
        gc.insets  = new Insets(0, 40, 6, 40);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(ERROR_RED);
        card.add(statusLabel, gc);

        // Login Button
        gc.gridy   = 9;
        gc.insets  = new Insets(0, 40, 12, 40);
        JButton loginBtn = makeButton("LOGIN", COEP_BLUE, Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(260, 42));
        loginBtn.addActionListener(e -> doLogin());
        card.add(loginBtn, gc);

        // Footer
        gc.gridy   = 10;
        gc.insets  = new Insets(20, 40, 10, 40);
        JLabel footer = makeLabel("© 2026 COEP Tech – All rights reserved",
                new Font("Segoe UI", Font.PLAIN, 11), new Color(0xAABBCC));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(footer, gc);

        return card;
    }

    // ── Login Logic ──────────────────────────────────────────────────────────

    /**
     * Called when the Login button is clicked or Enter is pressed in the password field.
     * Validates input, calls UserService.login(), checks role match, then navigates.
     */
    private void doLogin() {
        statusLabel.setText(" ");

        // ── Input validation ──────────────────────────────────────────────────
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            showError("Please enter your User ID.");
            idField.requestFocus();
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            showError("User ID must be a number.");
            idField.requestFocus();
            return;
        }

        String password = new String(passField.getPassword());
        if (password.isEmpty()) {
            showError("Please enter your password.");
            passField.requestFocus();
            return;
        }

        String selectedRole = (String) roleCombo.getSelectedItem();

        // ── Authenticate via UserService (existing logic) ─────────────────────
        User user = userService.login(userId, password);

        if (user == null) {
            showError("Invalid ID or password. Please try again.");
            passField.setText("");
            passField.requestFocus();
            return;
        }

        // ── Role match check ──────────────────────────────────────────────────
        if (!user.getRole().equalsIgnoreCase(selectedRole)) {
            showError("This account does not have role: " + selectedRole);
            passField.setText("");
            return;
        }

        // ── Success — hand off to MainFrame ───────────────────────────────────
        statusLabel.setForeground(SUCCESS_GREEN);
        statusLabel.setText("✓  Welcome, " + user.getName() + "!");
        passField.setText("");

        // Short delay for feedback, then navigate
        Timer timer = new Timer(400, ev -> frame.showDashboard(user));
        timer.setRepeats(false);
        timer.start();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String msg) {
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setText("✗  " + msg);
    }

    static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static void styleTextField(JTextField tf, String placeholder) {
        tf.setPreferredSize(new Dimension(260, 38));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCD3DC), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
    }

    static void styleCombo(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(260, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
    }

    static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }
}
