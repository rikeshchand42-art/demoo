import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private JCheckBox rememberCheckBox;

    public LoginForm() {
        // Frame setup
        setTitle("Login Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Title
        JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 15));
        formPanel.setBackground(new Color(240, 240, 240));

        // Email/Username label and field
        JLabel emailLabel = new JLabel("Email/Username:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        emailField = new JTextField(15);
        emailField.setFont(new Font("Arial", Font.PLAIN, 12));

        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));

        // Remember checkbox
        rememberCheckBox = new JCheckBox("Remember me");
        rememberCheckBox.setFont(new Font("Arial", Font.PLAIN, 11));
        rememberCheckBox.setBackground(new Color(240, 240, 240));

        // Empty label for spacing
        JLabel emptyLabel = new JLabel();

        // Add components to form panel
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(rememberCheckBox);
        formPanel.add(emptyLabel);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 12));
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(new LoginButtonListener());

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setBackground(new Color(244, 67, 54));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(new ClearButtonListener());

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(Color.RED);

        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Center panel combining form, buttons, and status
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout(0, 10));
        centerPanel.setBackground(new Color(240, 240, 240));
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Allow Enter key to trigger login
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (getFocusOwner() instanceof JPasswordField || getFocusOwner() instanceof JTextField) {
                        loginButton.doClick();
                        return true;
                    }
                }
                return false;
            }
        });

        setVisible(true);
    }

    // Login button listener
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please fill in all fields!");
                statusLabel.setForeground(Color.RED);
            } else if (email.equals("admin") && password.equals("password")) {
                statusLabel.setText("Login successful!");
                statusLabel.setForeground(new Color(76, 175, 80));
                JOptionPane.showMessageDialog(null, "Welcome " + email + "!");
                if (rememberCheckBox.isSelected()) {
                    JOptionPane.showMessageDialog(null, "Credentials will be remembered.");
                }
            } else {
                statusLabel.setText("Invalid email/password!");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    // Clear button listener
    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            emailField.setText("");
            passwordField.setText("");
            rememberCheckBox.setSelected(false);
            statusLabel.setText(" ");
            emailField.requestFocus();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm();
            }
        });
    }
}
