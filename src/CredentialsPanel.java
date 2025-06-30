import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import java.util.List;

public class CredentialsPanel extends JPanel {

    private final JTextField websiteField = new JTextField(20);
    private final JTextField usernameField = new JTextField(20);
    private final JTextField passwordField = new JTextField(20);

    private final JButton addButton = new JButton("Add Credentials");
    private final JButton showButton = new JButton("Show Saved Passwords");

    private static final String CREDENTIALS_FILE = "saved_credentials.json";

    private SavedCredentialsViewer viewer = null;

    private final JLabel warningLabel = new JLabel("One or more of your passwords is close to this.");

    public CredentialsPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Website Field
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Website:"), gbc);
        gbc.gridx = 1;
        add(websiteField, gbc);

        // Username Field
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Username/Email:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Warning Label
        warningLabel.setForeground(new Color(153, 120, 0));
        warningLabel.setVisible(false);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(warningLabel, gbc);
        gbc.gridwidth = 1;

        // Password Field
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Add & Show Buttons
        gbc.gridx = 0; gbc.gridy = 4;
        add(addButton, gbc);
        gbc.gridx = 1;
        add(showButton, gbc);

        // Button Actions
        addButton.addActionListener(e -> saveCredential());
        showButton.addActionListener(e -> {
            if (viewer == null || !viewer.isDisplayable()) {
                viewer = new SavedCredentialsViewer();
            } else {
                viewer.toFront();
                viewer.requestFocus();
            }
        });

        // Password Similarity Listener
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String password = passwordField.getText().trim();
                if (!password.isEmpty()) {
                    PasswordSimilarityChecker.SimilarityStatus status = PasswordSimilarityChecker.checkPasswordSimilarity(password);
                    warningLabel.setVisible(status == PasswordSimilarityChecker.SimilarityStatus.ERROR1);
                } else {
                    warningLabel.setVisible(false);
                }
            }
        });
    }

    private void saveCredential() {
        String website = websiteField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("website", website);
        entry.put("username", username);
        entry.put("password", password);

        List<Map<String, String>> allEntries = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                Reader reader = Files.newBufferedReader(Paths.get(CREDENTIALS_FILE));
                allEntries = new ArrayList<>(Arrays.asList(gson.fromJson(reader, Map[].class)));
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        allEntries.add(entry);

        try (Writer writer = Files.newBufferedWriter(Paths.get(CREDENTIALS_FILE))) {
            gson.toJson(allEntries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        websiteField.setText("");
        usernameField.setText("");
        passwordField.setText("");

        warningLabel.setVisible(false);

        if (viewer != null && viewer.isDisplayable()) {
            viewer.refresh();
        }
    }
}
