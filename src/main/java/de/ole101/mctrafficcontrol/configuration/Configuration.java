package de.ole101.mctrafficcontrol.configuration;

import lombok.Data;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import static de.ole101.mctrafficcontrol.McTrafficControl.LOGGER;
import static de.ole101.mctrafficcontrol.McTrafficControl.MOD_ID;
import static de.ole101.mctrafficcontrol.utils.ModUtils.GSON;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

/*
 * https://github.com/rettichlp/the-rettington-companion/blob/develop/src/main/java/de/rettichlp/therettingtoncompanion/configuration/Configuration.java
 */
@Data
public class Configuration {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");

    public Configuration loadFromFile() {
        File file = CONFIG_PATH.toFile();

        // create a new config if the file does not exist or is empty
        if (!file.exists() || file.length() == 0) {
            LOGGER.info("Config file does not exist or is empty, creating new one at {}", CONFIG_PATH);
            saveToFile();
            return this;
        }

        // load existing config
        try {
            Reader reader = newBufferedReader(CONFIG_PATH);
            Configuration configuration = GSON.fromJson(reader, Configuration.class);
            LOGGER.info("Loaded configuration: {}", configuration);
            return configuration;
        } catch (Exception e) {
            LOGGER.error("Failed to load config from {}", CONFIG_PATH, e);
        }

        // fallback
        LOGGER.warn("Failed to load config, using default values");
        saveToFile();

        return this;
    }

    public void saveToFile() {
        try (Writer writer = newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
            LOGGER.info("Saved config to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_PATH, e);
        }
    }
}
