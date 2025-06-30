import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

public class PassManagerPlus {

    static final String SETTINGS_FILE = "user_settings.json";

static class Settings {
    int width = 400;
    int height = 300;
    Integer x = null;
    Integer y = null;

    int viewerWidth = 500;
    int viewerHeight = 400;
    Integer viewerX = null;
    Integer viewerY = null;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Settings settings = loadSettings();

            JFrame frame = new JFrame("Pass Manager Plus");
            frame.setSize(settings.width, settings.height);

            if (settings.x != null && settings.y != null) {
                frame.setLocation(settings.x, settings.y);
            } else {
                frame.setLocationRelativeTo(null); // Center
            }

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Save settings on close
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    saveSettings(frame);
                }
            });

            frame.add(new CredentialsPanel());

            frame.setVisible(true);
        });
    }

    static Settings loadSettings() {
        try {
            String json = Files.readString(Path.of(SETTINGS_FILE));
            return new Gson().fromJson(json, Settings.class);
        } catch (IOException e) {
            return new Settings(); // Default if not found
        }
    }

    static void saveSettings(JFrame frame) {
        Settings s = new Settings();
        Dimension size = frame.getSize();
        Point location = frame.getLocation();

        s.width = size.width;
        s.height = size.height;
        s.x = location.x;
        s.y = location.y;

        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(s, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static Settings getSettings() {
        return loadSettings();
    }

    public static void saveSettings(Settings s) {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(s, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
