import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PasswordSimilarityChecker {

    public enum SimilarityStatus {
        OK,
        ERROR1 // One or more passwords are too similar
    }

    private static final String CREDENTIALS_FILE = "saved_credentials.json";

    public static SimilarityStatus checkPasswordSimilarity(String inputPassword) {
        List<String> existingPasswords = new ArrayList<>();

        try {
            if (Files.exists(Paths.get(CREDENTIALS_FILE))) {
                Reader reader = Files.newBufferedReader(Paths.get(CREDENTIALS_FILE));
                Gson gson = new Gson();
                List<Map<String, String>> allEntries = Arrays.asList(gson.fromJson(reader, Map[].class));
                reader.close();

                for (Map<String, String> entry : allEntries) {
                    String password = entry.get("password");
                    if (password != null) {
                        existingPasswords.add(password);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String saved : existingPasswords) {
            if (isSimilar(inputPassword, saved)) {
                return SimilarityStatus.ERROR1;
            }
        }

        return SimilarityStatus.OK;
    }

    private static boolean isSimilar(String input, String existing) {
        if (input.equalsIgnoreCase(existing)) return true;
        if (Math.abs(input.length() - existing.length()) > 2) return false;

        int distance = levenshteinDistance(input, existing);
        return distance <= 2; // Adjustable threshold
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++)
            for (int j = 0; j <= b.length(); j++)
                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else
                    dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));

        return dp[a.length()][b.length()];
    }
}
