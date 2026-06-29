package dev.alicon.mushroomyorkie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.alicon.mushroomyorkie.spawn.YorkieSpawnMode;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

final class MushroomYorkieConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("mushroom_yorkie.json");

	private final YorkieSpawnMode spawnMode;
	private final boolean spawnAfterSuccessfulSleep;
	private final boolean oneMushroomPerPlayer;

	private MushroomYorkieConfig(YorkieSpawnMode spawnMode, boolean spawnAfterSuccessfulSleep, boolean oneMushroomPerPlayer) {
		this.spawnMode = spawnMode;
		this.spawnAfterSuccessfulSleep = spawnAfterSuccessfulSleep;
		this.oneMushroomPerPlayer = oneMushroomPerPlayer;
	}

	static MushroomYorkieConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			MushroomYorkieConfig config = defaults();
			config.save();
			return config;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			ConfigFile file = GSON.fromJson(reader, ConfigFile.class);
			if (file == null) {
				return defaults();
			}

				return new MushroomYorkieConfig(
						YorkieSpawnMode.fromConfigValue(file.wakeUpSpawnMode),
						file.spawnAfterSuccessfulSleep,
						file.oneMushroomPerPlayer == null || file.oneMushroomPerPlayer
				);
		} catch (IOException exception) {
			MushroomTheYorkie.LOGGER.warn("Failed to read Mushroom the Yorkie config, using defaults", exception);
			return defaults();
		}
	}

	YorkieSpawnMode spawnMode() {
		return this.spawnMode;
	}

	boolean spawnAfterSuccessfulSleep() {
		return this.spawnAfterSuccessfulSleep;
	}

	boolean oneMushroomPerPlayer() {
		return this.oneMushroomPerPlayer;
	}

	private static MushroomYorkieConfig defaults() {
		return new MushroomYorkieConfig(YorkieSpawnMode.RESPAWN, true, true);
	}

	private void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(ConfigFile.from(this), writer);
			}
		} catch (IOException exception) {
			MushroomTheYorkie.LOGGER.warn("Failed to write default Mushroom the Yorkie config", exception);
		}
	}

	private static final class ConfigFile {
		String wakeUpSpawnMode = YorkieSpawnMode.RESPAWN.configValue();
		boolean spawnAfterSuccessfulSleep = true;
		Boolean oneMushroomPerPlayer = true;

		private static ConfigFile from(MushroomYorkieConfig config) {
			ConfigFile file = new ConfigFile();
			file.wakeUpSpawnMode = config.spawnMode.configValue();
			file.spawnAfterSuccessfulSleep = config.spawnAfterSuccessfulSleep;
			file.oneMushroomPerPlayer = config.oneMushroomPerPlayer;
			return file;
		}
	}
}
