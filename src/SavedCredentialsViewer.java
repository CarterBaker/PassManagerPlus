import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class SavedCredentialsViewer extends JFrame {
    private static final String CREDENTIALS_FILE = "saved_credentials.json";
    private JPanel credentialsPanel;

    public SavedCredentialsViewer() {
        PassManagerPlus.Settings settings = PassManagerPlus.getSettings();

        setTitle("Saved Credentials");
        setSize(settings.viewerWidth, settings.viewerHeight);

        if (settings.viewerX != null && settings.viewerY != null) {
            setLocation(settings.viewerX, settings.viewerY);
        } else {
            setLocationRelativeTo(null);
        }

        credentialsPanel = new JPanel();
        credentialsPanel.setLayout(new BoxLayout(credentialsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(credentialsPanel);
        add(scrollPane);

        // Save viewer window size/position on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveViewerWindowState();
            }
        });

        loadCredentials();
        setVisible(true);
    }

    public void refresh() {
        loadCredentials();
    }

    private void saveViewerWindowState() {
        PassManagerPlus.Settings s = PassManagerPlus.getSettings();
        Dimension size = getSize();
        Point location = getLocation();

        s.viewerWidth = size.width;
        s.viewerHeight = size.height;
        s.viewerX = location.x;
        s.viewerY = location.y;

        PassManagerPlus.saveSettings(s);
    }

    private List<Map<String, String>> readCredentials() {
        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(Paths.get(CREDENTIALS_FILE));
                List<Map<String, String>> list = gson.fromJson(reader, new TypeToken<List<Map<String, String>>>(){}.getType());
                reader.close();
                return list;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private void saveCredentials(List<Map<String, String>> credentials) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(CREDENTIALS_FILE))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(credentials, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCredentials() {
        credentialsPanel.removeAll();
        List<Map<String, String>> credentials = readCredentials();

        for (int i = 0; i < credentials.size(); i++) {
            Map<String, String> entry = credentials.get(i);
            int index = i;

            JPanel entryPanel = new JPanel(new GridLayout(0, 1));
            entryPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createLineBorder(Color.GRAY)));

            JLabel websiteLabel = new JLabel("Website: " + entry.get("website"));
            JLabel usernameLabel = new JLabel("Username: " + entry.get("username"));
            JLabel passwordLabel = new JLabel("Password: " + "*".repeat(entry.get("password").length()));

            JButton toggleButton = new JButton("Show Password");
            JButton deleteButton = new JButton("Delete");

            toggleButton.addActionListener(e -> {
                if (toggleButton.getText().equals("Show Password")) {
                    passwordLabel.setText("Password: " + entry.get("password"));
                    toggleButton.setText("Hide Password");
                } else {
                    passwordLabel.setText("Password: " + "*".repeat(entry.get("password").length()));
                    toggleButton.setText("Show Password");
                }
            });

            deleteButton.addActionListener(e -> {
                credentials.remove(index);
                saveCredentials(credentials);
                loadCredentials(); // Refresh UI
                revalidate();
                repaint();
            });

            entryPanel.add(websiteLabel);
            entryPanel.add(usernameLabel);
            entryPanel.add(passwordLabel);
            entryPanel.add(toggleButton);
            entryPanel.add(deleteButton);
            credentialsPanel.add(entryPanel);
        }

        credentialsPanel.revalidate();
        credentialsPanel.repaint();
    }
}
