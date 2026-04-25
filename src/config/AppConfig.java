package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppConfig {

    private static final String CONFIG_FILE = "config/chatapp.properties";
    private static final Properties FILE_PROPERTIES = loadProperties();

    private AppConfig() {
    }

    public static String getRequiredString(String key, String envName) {
        String value = getString(key, envName, null);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Missing required configuration for " + key + ". Update " + CONFIG_FILE + " before running the app.");
        }

        return value;
    }

    public static String getString(String key, String envName, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (hasText(systemValue)) {
            return systemValue.trim();
        }

        String envValue = System.getenv(envName);
        if (hasText(envValue)) {
            return envValue.trim();
        }

        String fileValue = FILE_PROPERTIES.getProperty(key);
        if (fileValue != null) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    public static int getInt(String key, String envName, int defaultValue) {
        String value = getString(key, envName, String.valueOf(defaultValue));

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid number for " + key + ": " + value, e);
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path configPath = Paths.get(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read " + CONFIG_FILE + ".", e);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
